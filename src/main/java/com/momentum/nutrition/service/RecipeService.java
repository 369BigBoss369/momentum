package com.momentum.nutrition.service;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.exception.nutrition.CustomFoodAlreadyExists;
import com.momentum.exception.nutrition.EmptyRecipeException;
import com.momentum.exception.nutrition.FoodNotFoundException;
import com.momentum.exception.UnauthorizedResourceAccessException;
import com.momentum.fitness.model.enums.SourceType;
import com.momentum.nutrition.dto.CreateRecipeDTO;
import com.momentum.nutrition.dto.CreateStepDTO;
import com.momentum.nutrition.dto.RecipeIngredientDTO;
import com.momentum.nutrition.dto.FoodSearchView;
import com.momentum.nutrition.dto.enums.OwnershipType;
import com.momentum.nutrition.dto.enums.FoodItemType;
import com.momentum.nutrition.model.Recipe;
import com.momentum.nutrition.model.RecipeIngredient;
import com.momentum.nutrition.model.Step;
import com.momentum.nutrition.model.enums.CompositeFoodType;
import com.momentum.nutrition.repository.RecipeRepository;
import com.momentum.security.AuthenticationMetadata;
import com.momentum.user.model.User;
import com.momentum.user.service.NutritionActivityService;
import com.momentum.user.service.UserService;
import com.momentum.util.AccessControlUtil;
import com.momentum.util.ImagePathResolver;
import com.momentum.util.ModerationUtil;
import com.momentum.util.NutritionMath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final FoodService foodService;
    private final UserService userService;
    private final NutritionActivityService nutritionActivityService;

    @Autowired
    public RecipeService(RecipeRepository recipeRepository, FoodService foodService, UserService userService, NutritionActivityService nutritionActivityService) {
        this.recipeRepository = recipeRepository;
        this.foodService = foodService;
        this.userService = userService;
        this.nutritionActivityService = nutritionActivityService;
    }

    @Transactional
    public Recipe createRecipe(CreateRecipeDTO createRecipeDTO, UUID userId) {
        Optional<Recipe> optionalRecipe = recipeRepository.findByOwnerIdAndName(userId, createRecipeDTO.getTitle());
        if (optionalRecipe.isPresent()) {
            throw new CustomFoodAlreadyExists(String.format("You have already added recipe with the name '%s'", createRecipeDTO.getTitle()));
        }

        if (createRecipeDTO.getIngredients().isEmpty()) {
            throw new EmptyRecipeException("The recipe has no ingredients");
        }

        Recipe recipe = Recipe.builder()
                .name(createRecipeDTO.getTitle())
                .type(createRecipeDTO.getType())
                .imagePath(
                        createRecipeDTO.getImagePath() == null || createRecipeDTO.getImagePath().isEmpty() ?
                                ImagePathResolver.resolveCompositeFoodImage(createRecipeDTO.getType())
                                : createRecipeDTO.getImagePath()
                )
                .isPublic(createRecipeDTO.getIsPublic())
                .ownerId(userId)
                .source(SourceType.CUSTOM)
                .build();

        ModerationUtil.applyPublicityChange(recipe, false);

        recipe.setIngredients(getIngredients(createRecipeDTO, recipe));
        recipe.setSteps(getSteps(createRecipeDTO));

        NutritionMath.calculateRecipeNutrition(recipe, createRecipeDTO.getServingSize());

        Recipe saved = recipeRepository.save(recipe);
        nutritionActivityService.logFoodCreated(userId, recipe.getName(), "recipe");
        return saved;
    }

    @Transactional
    public Recipe updateRecipe(UUID id, CreateRecipeDTO createRecipeDTO, UUID userId) {
        Recipe recipe = recipeRepository.findById(id).orElseThrow(() -> new FoodNotFoundException("Recipe does not exist"));

        if (!userId.equals(recipe.getOwnerId())) {
            throw new UnauthorizedResourceAccessException("You do not have permission to modify this food");
        }

        Optional<Recipe> optional = recipeRepository.findByOwnerIdAndName(userId, createRecipeDTO.getTitle());
        if (optional != null && optional.isPresent() && !optional.get().getId().equals(id)) {
            throw new CustomFoodAlreadyExists(String.format("You have already added recipe with the name '%s'", createRecipeDTO.getTitle()));
        }

        if (createRecipeDTO.getIngredients().isEmpty()) {
            throw new EmptyRecipeException("The recipe has no ingredients");
        }

        recipe.setName(createRecipeDTO.getTitle());
        recipe.setType(createRecipeDTO.getType());
        recipe.setImagePath(
                createRecipeDTO.getImagePath() == null || createRecipeDTO.getImagePath().isEmpty()
                        ? ImagePathResolver.resolveCompositeFoodImage(createRecipeDTO.getType())
                        : createRecipeDTO.getImagePath()
        );

        boolean wasPublic = Boolean.TRUE.equals(recipe.getIsPublic());
        recipe.setIsPublic(createRecipeDTO.getIsPublic());
        ModerationUtil.applyPublicityChange(recipe, wasPublic);

        recipe.getIngredients().clear();
        recipe.getIngredients().addAll(getIngredients(createRecipeDTO, recipe));

        recipe.getSteps().clear();
        recipe.getSteps().addAll(getSteps(createRecipeDTO));

        NutritionMath.calculateRecipeNutrition(recipe, createRecipeDTO.getServingSize());

        Recipe saved = recipeRepository.save(recipe);
        nutritionActivityService.logFoodUpdated(userId, recipe.getName(), "recipe");
        return saved;
    }

    private Set<RecipeIngredient> getIngredients(CreateRecipeDTO createRecipeDTO, Recipe recipe) {
        Set<RecipeIngredient> recipeIngredients = new LinkedHashSet<>();

        for (RecipeIngredientDTO recipeIngredientDTO : createRecipeDTO.getIngredients()) {
            recipeIngredients.add(
                    RecipeIngredient.builder()
                            .recipe(recipe)
                            .food(foodService.getById(recipeIngredientDTO.getFoodId()))
                            .servingSize(recipeIngredientDTO.getServingSize())
                            .build()
            );
        }

        return recipeIngredients;
    }

    private static Set<Step> getSteps(CreateRecipeDTO createRecipeDTO) {
        Set<Step> steps = new LinkedHashSet<>();

        for (CreateStepDTO stepDTO : createRecipeDTO.getSteps()) {
            steps.add(
                    Step.builder()
                            .title(stepDTO.getTitle())
                            .description(stepDTO.getDescription())
                            .imageUrl(stepDTO.getImageUrl())
                            .stepNumber(stepDTO.getStepNumber())
                            .build()
            );
        }

        return steps.stream()
                .sorted(Comparator.comparing(Step::getStepNumber))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public List<FoodSearchView> search(String name, List<UUID> ingredients, OwnershipType ownership, UUID userId, Boolean inLibrary) {
        String normalizedName = (name == null || name.trim().isEmpty()) ? null : name.trim();
        List<UUID> normalizedIngredients = (ingredients == null || ingredients.isEmpty()) ? null : ingredients;

        return recipeRepository.searchRecipes(
                        normalizedName,
                        userId,
                        ownership.name(),
                        normalizedIngredients,
                        inLibrary
                ).stream()
                .map(recipe -> {
                    FoodSearchView food = FoodSearchView.from(recipe, userId);

                    food.setItemType(FoodItemType.RECIPE);
                    food.setFoodType(recipe.getType().getDisplayName());
                    food.setIngredientCount(recipe.getIngredients().size());

                    return food;
                })
                .toList();
    }


    @Transactional
    public void addToLibrary(UUID recipeId, UUID userId) {
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(() -> new FoodNotFoundException("Recipe does not exist"));

        if (!Boolean.TRUE.equals(recipe.getIsPublic()) || !ModerationUtil.isVisible(recipe)) {
            throw new IllegalStateException("This item is not yet approved and cannot be added to a library");
        }

        User user = userService.getById(userId);

        boolean isShared = recipe.getSharedUsers().stream().anyMatch(u -> u.getId().equals(userId));

        if (!isShared) {
            recipe.getSharedUsers().add(user);
            recipeRepository.save(recipe);

            nutritionActivityService.logRecipeAddedToLibrary(userId, recipe.getName());
        }
    }

    @Transactional
    public void removeFromLibrary(UUID recipeId, UUID userId) {
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(() -> new FoodNotFoundException("Recipe does not exist"));

        boolean removed = recipe.getSharedUsers().removeIf(u -> u.getId().equals(userId));
        
        if (removed) {
            recipeRepository.save(recipe);
            nutritionActivityService.logRecipeRemovedFromLibrary(userId, recipe.getName());
        }
    }

    public Recipe getById(UUID recipeId, User currentUser) {
        Recipe recipe = recipeRepository.findRecipeById(recipeId).orElseThrow(() -> new FoodNotFoundException("Recipe does not exist"));

        if (!AccessControlUtil.canView(recipe, currentUser)) {
            throw new FoodNotFoundException("Recipe food does not exist");
        }

        return recipe;
    }

    public boolean isOwner(Recipe recipe, UUID userId) {
        return userId.equals(recipe.getOwnerId());
    }

    public String getOwnerUsernameIfNotOwner(Recipe recipe, UUID userId) {
        if (isOwner(recipe, userId)) {
            return null;
        }

        return userService.getById(recipe.getOwnerId()).getUsername();
    }

    public int getSharedUsersCount(Recipe recipe) {
        return Optional.ofNullable(recipe.getSharedUsers())
                .map(Set::size)
                .orElse(0);
    }

    public boolean isInLibrary(Recipe recipe, UUID userId) {
        return Optional.ofNullable(recipe.getSharedUsers())
                .map(users -> users.stream().anyMatch(u -> u.getId().equals(userId)))
                .orElse(false);
    }

    public List<Recipe> getPendingApproval() {
        return recipeRepository.findByIsPublicTrueAndModerationStatus(ModerationStatus.PENDING);
    }

    @Transactional
    public void approve(UUID id) {
        Recipe recipe = recipeRepository.findById(id).orElseThrow(() -> new FoodNotFoundException("Recipe does not exist"));
        recipe.setModerationStatus(ModerationStatus.APPROVED);
        recipeRepository.save(recipe);
    }

    @Transactional
    public void reject(UUID id) {
        Recipe recipe = recipeRepository.findById(id).orElseThrow(() -> new FoodNotFoundException("Recipe does not exist"));
        recipe.setModerationStatus(ModerationStatus.REJECTED);
        recipe.setIsPublic(false);
        recipeRepository.save(recipe);
    }

    public List<Recipe> getByType(CompositeFoodType type, UUID ownerId, UUID sharedUserId) {
        return recipeRepository.findByTypeAndOwnerIdAndSharedUsersId(type, ownerId, sharedUserId);
    }

    public long count() {
        return recipeRepository.count();
    }
}

