package com.momentum.nutrition.model.enums;

import lombok.Getter;

@Getter

public enum ProductType {
    PORK("Pork"),
    BEEF("Beef"),
    VEAL("Veal"),
    LAMB("Lamb"),
    GAME("Game"),
    POULTRY("Poultry"),
    FISH("Fish"),
    VEGETABLE("Vegetable"),
    LEGUME("Legume"),
    FRUIT("Fruit"),
    GRAIN("Grain"),
    DAIRY("Dairy"),
    EGG("Egg"),
    FATS_AND_OILS("Fats and Oils"),
    NUTS_AND_SEEDS("Nuts and Seeds"),
    SPICES_AND_HERBS("Spices and Herbs");

    private final String displayName;

    ProductType(String displayName) {
        this.displayName = displayName;
    }
}

