package com.momentum.user.model.enums;

import lombok.Getter;



@Getter

public enum UserGoal {
    MAINTAIN_WEIGHT("Maintain Weight"),
    LOSE_WEIGHT("Lose Weight"),
    RECOMPOSITION("Recomposition"),
    GAIN_MUSCLE("Gain Muscle");

    private final String displayName;

    UserGoal(String displayName) {
        this.displayName = displayName;
    }
}

