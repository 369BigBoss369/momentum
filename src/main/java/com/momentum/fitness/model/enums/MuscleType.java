package com.momentum.fitness.model.enums;

import lombok.Getter;

@Getter

public enum MuscleType {
    NECK,
    UPPER_TRAPS, MIDDLE_TRAPS, LOWER_TRAPS, RHOMBOIDS, TERES_MAJOR, LATS,
    FRONT_DELTOID, LATERAL_DELTOID, REAR_DELTOID, ROTATOR_CUFF,
    LONG_HEAD_TRICEPS, MEDIAL_HEAD_TRICEPS, LATERAL_HEAD_TRICEPS,
    SHORT_HEAD_BICEPS, LONG_HEAD_BICEPS, BRACHIALIS,
    BRACHIORADIALIS, FLEXORS, EXTENSORS,
    UPPER_CHEST, MIDDLE_CHEST, LOWER_CHEST, SERRATUS,
    UPPER_ABS, LOWER_ABS, OBLIQUES, LOWER_BACK,
    GLUTES, HIP_FLEXORS, ABDUCTOR, QUADRICEPS, HAMSTRINGS, ADDUCTORS, CALVES;

    private final String displayName;

    MuscleType() {
        this.displayName = formatDisplayName(this.name());
    }

    private static String formatDisplayName(String raw) {
        String cleaned = raw
                .replace("TRICEPS", "")
                .replace("BICEPS", "")
                .trim()
                .replace("_", " ")
                .toLowerCase();

        String[] words = cleaned.split(" ");
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }

            sb.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }

        return sb.toString().trim();
    }
}

