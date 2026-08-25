package com.momentum.fitness.model.enums;

import lombok.Getter;

@Getter

public enum Intensity {
    LOW("Low", 3.0),
    MODERATE("Moderate", 3.5),
    HIGH("High", 6.0),
    EXTREME("Extreme", 8.0);

    private final String displayName;
    private final double met;

    Intensity(String displayName, double met) {
        this.displayName = displayName;
        this.met = met;
    }
}

