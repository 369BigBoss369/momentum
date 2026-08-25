package com.momentum.util;

import com.momentum.nutrition.model.enums.CompositeFoodType;
import com.momentum.nutrition.model.enums.ProductType;

public class ImagePathResolver {
    private static final String PRODUCT_PATH = "/images/products/";
    private static final String COMPOSITE_PATH = "/images/composites/";

    public static String resolveProductImage(ProductType type) {
        return switch (type) {
            case PORK -> PRODUCT_PATH + "pork.png";
            case BEEF -> PRODUCT_PATH + "beef.png";
            case VEAL -> PRODUCT_PATH + "veal.png";
            case LAMB -> PRODUCT_PATH + "lamb.png";
            case GAME -> PRODUCT_PATH + "game.png";
            case POULTRY -> PRODUCT_PATH + "poultry.png";
            case FISH -> PRODUCT_PATH + "fish.png";
            case VEGETABLE -> PRODUCT_PATH + "vegetable.png";
            case LEGUME -> PRODUCT_PATH + "legume.png";
            case FRUIT -> PRODUCT_PATH + "fruit.png";
            case GRAIN -> PRODUCT_PATH + "grain.png";
            case DAIRY -> PRODUCT_PATH + "dairy.png";
            case EGG -> PRODUCT_PATH + "egg.png";
            case FATS_AND_OILS -> PRODUCT_PATH + "fats_and_oils.png";
            case NUTS_AND_SEEDS -> PRODUCT_PATH + "nuts_and_seeds.png";
            case SPICES_AND_HERBS -> PRODUCT_PATH + "spices_and_herbs.png";
        };
    }

    public static String resolveCompositeFoodImage(CompositeFoodType type) {
        return switch (type) {
            case MEALS_AND_SIDE_DISHES -> COMPOSITE_PATH + "meals_and_side_dishes.png";
            case SOUPS_AND_SAUCES -> COMPOSITE_PATH + "soups_and_sauces.png";
            case FAST_FOOD -> COMPOSITE_PATH + "fast_food.png";
            case PROCESSED_MEAT -> COMPOSITE_PATH + "processed_meat.png";
            case PASTRY -> COMPOSITE_PATH + "pastry.png";
            case SNACKS -> COMPOSITE_PATH + "snacks.png";
            case SWEETS -> COMPOSITE_PATH + "sweets.png";
            case BEVERAGES -> COMPOSITE_PATH + "beverages.png";
            case FOOD_REPLACEMENT -> COMPOSITE_PATH + "food_replacement.png";
        };
    }
}

