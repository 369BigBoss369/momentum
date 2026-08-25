package com.momentum.fitness.model.enums;

import lombok.Getter;

import java.util.List;

@Getter

public enum MuscleGroup {
    NECK(List.of(MuscleType.NECK), "Neck"),

    BACK(List.of(
            MuscleType.UPPER_TRAPS, MuscleType.MIDDLE_TRAPS, MuscleType.LOWER_TRAPS,
            MuscleType.RHOMBOIDS, MuscleType.TERES_MAJOR, MuscleType.LATS
    ), "Back"),

    SHOULDERS(List.of(
            MuscleType.FRONT_DELTOID, MuscleType.LATERAL_DELTOID,
            MuscleType.REAR_DELTOID, MuscleType.ROTATOR_CUFF
    ), "Shoulders"),

    TRICEPS(List.of(
            MuscleType.LONG_HEAD_TRICEPS, MuscleType.MEDIAL_HEAD_TRICEPS, MuscleType.LATERAL_HEAD_TRICEPS
    ), "Triceps"),

    BICEPS(List.of(
            MuscleType.SHORT_HEAD_BICEPS, MuscleType.LONG_HEAD_BICEPS, MuscleType.BRACHIALIS
    ), "Biceps"),

    FOREARMS(List.of(
            MuscleType.BRACHIORADIALIS, MuscleType.FLEXORS, MuscleType.EXTENSORS
    ), "Forearms"),

    CHEST(List.of(
            MuscleType.UPPER_CHEST, MuscleType.MIDDLE_CHEST,
            MuscleType.LOWER_CHEST, MuscleType.SERRATUS
    ), "Chest"),

    CORE(List.of(
            MuscleType.UPPER_ABS, MuscleType.LOWER_ABS, MuscleType.OBLIQUES, MuscleType.LOWER_BACK
    ), "Core"),

    LEGS(List.of(
            MuscleType.GLUTES, MuscleType.HIP_FLEXORS, MuscleType.ABDUCTOR,
            MuscleType.QUADRICEPS, MuscleType.HAMSTRINGS, MuscleType.ADDUCTORS, MuscleType.CALVES
    ), "Legs");

    private final List<MuscleType> muscles;
    private final String displayName;

    MuscleGroup(List<MuscleType> muscles, String displayName) {
        this.muscles = muscles;
        this.displayName = displayName;
    }

}

