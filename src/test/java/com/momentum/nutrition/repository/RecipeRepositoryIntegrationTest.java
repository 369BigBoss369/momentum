package com.momentum.nutrition.repository;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.fitness.model.enums.SourceType;
import com.momentum.nutrition.model.Recipe;
import com.momentum.nutrition.model.enums.CompositeFoodType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RecipeRepositoryIntegrationTest {

    @Autowired
    private RecipeRepository recipeRepository;

    private Recipe buildRecipe(String name, UUID ownerId, SourceType source, boolean isPublic, ModerationStatus status) {
        Recipe recipe = new Recipe();
        recipe.setName(name);
        recipe.setType(CompositeFoodType.MEALS_AND_SIDE_DISHES);
        recipe.setCalories(400);
        recipe.setOwnerId(ownerId);
        recipe.setSource(source);
        recipe.setIsPublic(isPublic);
        recipe.setModerationStatus(status);
        return recipe;
    }

    @Test
    void save_ShouldPersistRecipe() {
        UUID ownerId = UUID.randomUUID();
        Recipe recipe = buildRecipe("Chicken Salad", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED);

        Recipe saved = recipeRepository.save(recipe);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Chicken Salad");
        assertThat(saved.getIngredients()).isEmpty();
        assertThat(saved.getSteps()).isEmpty();
    }

    @Test
    void findByOwnerIdAndName_ShouldReturnRecipe_WhenExists() {
        UUID ownerId = UUID.randomUUID();
        recipeRepository.save(buildRecipe("Beef Stew", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));

        Optional<Recipe> found = recipeRepository.findByOwnerIdAndName(ownerId, "Beef Stew");

        assertThat(found).isPresent();
    }

    @Test
    void findByIsPublicTrueAndModerationStatus_ShouldReturnOnlyMatching() {
        UUID ownerId = UUID.randomUUID();
        recipeRepository.save(buildRecipe("Pending Recipe", ownerId, SourceType.CUSTOM, true, ModerationStatus.PENDING));
        recipeRepository.save(buildRecipe("Approved Recipe", ownerId, SourceType.CUSTOM, true, ModerationStatus.APPROVED));

        List<Recipe> pending = recipeRepository.findByIsPublicTrueAndModerationStatus(ModerationStatus.PENDING);

        assertThat(pending).extracting(Recipe::getName).containsExactly("Pending Recipe");
    }

    @Test
    void findRecipeById_ShouldFetchIngredientsAndStepsEagerly() {
        UUID ownerId = UUID.randomUUID();
        Recipe saved = recipeRepository.save(buildRecipe("Pasta Bake", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));

        Optional<Recipe> found = recipeRepository.findRecipeById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getIngredients()).isNotNull();
        assertThat(found.get().getSteps()).isNotNull();
    }

    @Test
    void searchRecipes_ShouldReturnOwnedRecipesForOwnershipOwn() {
        UUID ownerId = UUID.randomUUID();
        recipeRepository.save(buildRecipe("My Recipe", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));

        List<Recipe> results = recipeRepository.searchRecipes(null, ownerId, "OWN", null, null);

        assertThat(results).extracting(Recipe::getName).contains("My Recipe");
    }
}
