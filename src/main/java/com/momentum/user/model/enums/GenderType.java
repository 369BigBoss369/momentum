package com.momentum.user.model.enums;

import lombok.Getter;

@Getter

public enum GenderType {
    MALE("Male"),
    FEMALE("Female");

    private final String displayName;

    GenderType(String displayName) {
        this.displayName = displayName;
    }
}

