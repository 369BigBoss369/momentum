package com.momentum.fitness.model.enums;

import lombok.Getter;

@Getter
public enum PlanDayType {
    ACTIVE("Active"),
    RECOVERY("Recovery"),
    REST("Rest");

    private final String displayName;

    PlanDayType(String displayName) {
        this.displayName = displayName;
    }
}

