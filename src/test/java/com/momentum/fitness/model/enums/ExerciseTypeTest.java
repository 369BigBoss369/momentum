package com.momentum.fitness.model.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExerciseTypeTest {

    @Test
    void exerciseTypeValues_ShouldContainExpectedValues() {
        ExerciseType[] values = ExerciseType.values();
        assertTrue(values.length > 0);

        // Check that STRENGTH type exists
        boolean hasStrength = false;
        for (ExerciseType type : values) {
            if ("STRENGTH".equals(type.name())) {
                hasStrength = true;
                break;
            }
        }
        assertTrue(hasStrength, "ExerciseType should contain STRENGTH");
    }

    @Test
    void exerciseTypeValueOf_ShouldWork() {
        assertDoesNotThrow(() -> ExerciseType.valueOf("STRENGTH"));
    }
}

