package com.momentum.fitness.model.enums;

import lombok.Getter;

@Getter

public enum WorkoutType {
    STRENGTH("Strength", 1.0),
    BODY_WEIGHT("Body Weight", 1.1),
    FULL_BODY("Full Body", 1.15),
    HIIT("HIIT", 1.35),
    CARDIO("Cardio", 1.2),
    MOBILITY("Mobility", 0.8),
    YOGA("Yoga", 0.7);

    private final String displayName;
    private final double metMultiplier;

    WorkoutType(String displayName, double metMultiplier) {
        this.displayName = displayName;
        this.metMultiplier = metMultiplier;
    }
}

