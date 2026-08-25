package com.momentum.fitness.model.enums;

import lombok.Getter;

@Getter

public enum ExerciseType {
    STRENGTH("Strength", 7.0),
    PLYOMETRIC("Plyometric", 10.0),
    CARDIO("Cardio", 9.0),
    AGILITY("Agility", 6.0),
    BALANCE("Balance", 6.0),
    FLEXIBILITY("Flexibility", 2.5),
    RECOVERY("Recovery", 1.8);

    private final String displayName;
    private final double baseMet;

    ExerciseType(String displayName, double baseMet) {
        this.displayName = displayName;
        this.baseMet = baseMet;
    }
}

