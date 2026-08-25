package com.momentum.fitness.model.enums;

import lombok.Getter;

@Getter

public enum PlanType {
    STRENGTH("Strength"),
    WEIGHT_LOSS("Weight Loss"),
    ENDURANCE("Endurance"),
    FLEXIBILITY("Flexibility");

    private final String displayName;

    PlanType(String displayName) {
        this.displayName = displayName;
    }
}

