package com.momentum.util;

import com.momentum.nutrition.dto.*;
import com.momentum.nutrition.model.CompositeFood;
import com.momentum.nutrition.model.Food;
import com.momentum.nutrition.model.Product;
import com.momentum.nutrition.model.Recipe;
import com.momentum.nutrition.model.Step;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class NutritionMapper {

    public static <T extends BaseFoodData, F extends Food> F mapToFood(T dto, F food) {
        T normalizedDto = normalizeMacros(dto);

        food.setName(normalizedDto.getName());
        food.setImagePath(normalizedDto.getImagePath());
        food.setServingSize(normalizedDto.getServingSize());

        food.setCalories(normalizedDto.getCalories());

        food.setCarbohydrates(normalizedDto.getCarbohydrates());
        food.setSugar(normalizedDto.getSugar());
        food.setFiber(normalizedDto.getFiber());
        food.setGlycemicIndex(normalizedDto.getGlycemicIndex());
        food.setGlycemicLoad(normalizedDto.getGlycemicLoad());

        food.setProtein(normalizedDto.getProtein());

        food.setFat(normalizedDto.getFat());
        food.setSaturatedFat(normalizedDto.getSaturatedFat());
        food.setMonoUnsaturated(normalizedDto.getMonoUnsaturated());
        food.setPolyUnsaturated(normalizedDto.getPolyUnsaturated());
        food.setTransFat(normalizedDto.getTransFat());
        food.setCholesterol(normalizedDto.getCholesterol());
        food.setCaffeine(normalizedDto.getCaffeine());
        food.setAlcohol(normalizedDto.getAlcohol());
        food.setSodium(normalizedDto.getSodium());
        food.setPotassium(normalizedDto.getPotassium());
        food.setCalcium(normalizedDto.getCalcium());
        food.setIron(normalizedDto.getIron());

        return food;
    }

    private static <T extends BaseFoodData> T normalizeMacros(T dto) {
        if (dto.getServingSize() == null || dto.getServingSize() == 100) {
            return dto;
        }

        NutritionMath.scaleNutrition(dto, 100.0 / dto.getServingSize());
        dto.setServingSize(100);
        return dto;
    }

    public static List<FoodSearchDTO> mapToFoodSearchDTO(List<? extends Food> foods) {
        return foods.stream()
                .map(FoodSearchDTO::from)
                .collect(Collectors.toList());
    }

    public static EditableFoodData fromProduct(Product product) {
        CreateProductDTO dto = CreateProductDTO.builder().build();

        fillCommonFields(dto, product);
        dto.setType(product.getType());
        dto.setIsPublic(product.getIsPublic());

        return dto;
    }

    public static EditableFoodData fromCompositeFood(CompositeFood compositeFood) {
        CreateCompositeFoodDTO dto = CreateCompositeFoodDTO.builder().build();

        fillCommonFields(dto, compositeFood);
        dto.setType(compositeFood.getType());
        dto.setIsPublic(compositeFood.getIsPublic());

        return dto;
    }

    public static CreateRecipeDTO fromRecipe(Recipe recipe) {

        List<RecipeIngredientDTO> ingredients = recipe.getIngredients().stream()
                .map(ri -> RecipeIngredientDTO.builder()
                        .foodId(ri.getFood().getId())
                        .servingSize(ri.getServingSize())
                        .foodName(ri.getFood().getName())
                        .imagePath(ri.getFood().getImagePath())
                        .build())
                .toList();

        List<CreateStepDTO> steps = recipe.getSteps().stream()
                .sorted(Comparator.comparing(Step::getStepNumber))
                .map(step -> CreateStepDTO.builder()
                        .title(step.getTitle())
                        .description(step.getDescription())
                        .imageUrl(step.getImageUrl())
                        .stepNumber(step.getStepNumber())
                        .build())
                .toList();

        String recipeImagePath = recipe.getImagePath();
        String formImagePath = recipeImagePath.startsWith("http://") || recipeImagePath.startsWith("https://")
                        ? recipeImagePath
                        : null;

        return CreateRecipeDTO.builder()
                .title(recipe.getName())
                .type(recipe.getType())
                .imagePath(formImagePath)
                .servingSize(100)
                .isPublic(recipe.getIsPublic())
                .ingredients(ingredients)
                .steps(steps)
                .build();
    }

    private static void fillCommonFields(BaseFoodData dto, Food food) {
        dto.setName(food.getName());
        String imagePath = food.getImagePath();
        if (imagePath != null && (imagePath.startsWith("http://") || imagePath.startsWith("https://"))) {
            dto.setImagePath(imagePath);
        } else {
            dto.setImagePath(null);
        }
        dto.setServingSize(100);
        dto.setCalories(food.getCalories());
        dto.setCarbohydrates(food.getCarbohydrates());
        dto.setSugar(food.getSugar());
        dto.setFiber(food.getFiber());
        dto.setGlycemicIndex(food.getGlycemicIndex());
        dto.setGlycemicLoad(food.getGlycemicLoad());
        dto.setProtein(food.getProtein());
        dto.setFat(food.getFat());
        dto.setSaturatedFat(food.getSaturatedFat());
        dto.setMonoUnsaturated(food.getMonoUnsaturated());
        dto.setPolyUnsaturated(food.getPolyUnsaturated());
        dto.setTransFat(food.getTransFat());
        dto.setCholesterol(food.getCholesterol());
        dto.setCaffeine(food.getCaffeine());
        dto.setAlcohol(food.getAlcohol());
        dto.setSodium(food.getSodium());
        dto.setPotassium(food.getPotassium());
        dto.setCalcium(food.getCalcium());
        dto.setIron(food.getIron());
    }
}

