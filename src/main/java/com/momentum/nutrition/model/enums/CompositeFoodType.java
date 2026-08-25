package com.momentum.nutrition.model.enums;

import lombok.Getter;

@Getter

public enum CompositeFoodType {
    MEALS_AND_SIDE_DISHES("Meals and Side Dishes"),
    SOUPS_AND_SAUCES("Soups and Sauces"),
    FAST_FOOD("Fast Food"),
    PROCESSED_MEAT("Processed Meat"),
    PASTRY("Pastry"),
    SNACKS("Snacks"),
    SWEETS("Sweets"),
    BEVERAGES("Beverages"),
    FOOD_REPLACEMENT("Food Replacement");

    private final String displayName;

    CompositeFoodType(String displayName) {
        this.displayName = displayName;
    }
}

