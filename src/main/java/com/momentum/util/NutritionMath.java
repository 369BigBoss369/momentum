package com.momentum.util;

import com.momentum.nutrition.dto.BaseFoodData;
import com.momentum.nutrition.model.Food;
import com.momentum.nutrition.model.Recipe;
import com.momentum.nutrition.model.RecipeIngredient;

public class NutritionMath {

    public static void calculateRecipeNutrition(Recipe recipe, int servingSize) {
        if (servingSize < 0) {
            throw new IllegalArgumentException("Serving size must be positive number");
        }

        int calories = 0;
        double carbs = 0;
        double sugar = 0;
        double fiber = 0;
        double gi = 0;
        double gl = 0;
        double protein = 0;
        double fat = 0;
        double satFat = 0;
        double mono = 0;
        double poly = 0;
        double trans = 0;
        double cholesterol = 0;
        double caffeine = 0;
        double alcohol = 0;
        double sodium = 0;
        double potassium = 0;
        double calcium = 0;
        double iron = 0;

        for (RecipeIngredient recipeIngredient : recipe.getIngredients()) {
            Food food = recipeIngredient.getFood();

            calories += zeroIfNull(food.getCalories());
            carbs += zeroIfNull(food.getCarbohydrates());
            sugar += zeroIfNull(food.getSugar());
            fiber += zeroIfNull(food.getFiber());
            gi += zeroIfNull(food.getGlycemicIndex());
            gl += zeroIfNull(food.getGlycemicLoad());
            protein += zeroIfNull(food.getProtein());
            fat += zeroIfNull(food.getFat());
            satFat += zeroIfNull(food.getSaturatedFat());
            mono += zeroIfNull(food.getMonoUnsaturated());
            poly += zeroIfNull(food.getPolyUnsaturated());
            trans += zeroIfNull(food.getTransFat());
            cholesterol += zeroIfNull(food.getCholesterol());
            caffeine += zeroIfNull(food.getCaffeine());
            alcohol += zeroIfNull(food.getAlcohol());
            sodium += zeroIfNull(food.getSodium());
            potassium += zeroIfNull(food.getPotassium());
            calcium += zeroIfNull(food.getCalcium());
            iron += zeroIfNull(food.getIron());
        }

        recipe.setCalories(calories);
        recipe.setCarbohydrates(carbs);
        recipe.setSugar(sugar);
        recipe.setFiber(fiber);
        recipe.setGlycemicIndex(gi);
        recipe.setGlycemicLoad(gl);
        recipe.setProtein(protein);
        recipe.setFat(fat);
        recipe.setSaturatedFat(satFat);
        recipe.setMonoUnsaturated(mono);
        recipe.setPolyUnsaturated(poly);
        recipe.setTransFat(trans);
        recipe.setCholesterol(cholesterol);
        recipe.setCaffeine(caffeine);
        recipe.setAlcohol(alcohol);
        recipe.setSodium(sodium);
        recipe.setPotassium(potassium);
        recipe.setCalcium(calcium);
        recipe.setIron(iron);

        scaleNutrition(recipe, 100.0 / servingSize);
    }

    public static <T extends BaseFoodData> void scaleNutrition(T food, double factor) {
        food.setCalories((int) Math.round(food.getCalories() * factor));
        food.setCarbohydrates(round(food.getCarbohydrates() * factor));
        food.setSugar(round(food.getSugar() * factor));
        food.setFiber(round(food.getFiber() * factor));
        food.setGlycemicIndex(roundIfNotNull(food.getGlycemicIndex(), factor));
        food.setGlycemicLoad(roundIfNotNull(food.getGlycemicLoad(), factor));
        food.setProtein(round(food.getProtein() * factor));
        food.setFat(round(food.getFat() * factor));
        food.setSaturatedFat(round(food.getSaturatedFat() * factor));
        food.setMonoUnsaturated(roundIfNotNull(food.getMonoUnsaturated(), factor));
        food.setPolyUnsaturated(roundIfNotNull(food.getPolyUnsaturated(), factor));
        food.setTransFat(roundIfNotNull(food.getTransFat(), factor));
        food.setCholesterol(roundIfNotNull(food.getCholesterol(), factor));
        food.setCaffeine(roundIfNotNull(food.getCaffeine(), factor));
        food.setAlcohol(roundIfNotNull(food.getAlcohol(), factor));
        food.setSodium(round(food.getSodium() * factor));
        food.setPotassium(roundIfNotNull(food.getPotassium(), factor));
        food.setCalcium(roundIfNotNull(food.getCalcium(), factor));
        food.setIron(roundIfNotNull(food.getIron(), factor));
    }

    private static double zeroIfNull(Number n) {
        return n == null ? 0.0 : n.doubleValue();
    }

    private static Double roundIfNotNull(Double value, double factor) {
        return value == null ? null : round(value * factor);
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}

