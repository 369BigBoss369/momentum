package com.momentum.util;

import com.momentum.fitness.dto.CreateWorkoutExerciseDTO;
import com.momentum.fitness.model.Exercise;
import com.momentum.fitness.model.MuscleTarget;
import com.momentum.fitness.model.enums.ExerciseType;
import com.momentum.fitness.model.enums.Intensity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

class FitnessMathTest {

    @Test
    void calculateBurnedCalories_ForStrengthExercise_ShouldReturnCorrectValue() {
        Exercise exercise = Exercise.builder()
                .id(UUID.randomUUID())
                .type(ExerciseType.STRENGTH)
                .muscleGroupTarget(List.of(MuscleTarget.builder().intensity(Intensity.MODERATE).build()))
                .build();

        CreateWorkoutExerciseDTO dto = CreateWorkoutExerciseDTO.builder()
                .reps(10)
                .weight(50.0)
                .build();

        Double result = FitnessMath.calculateBurnedCalories(exercise, dto);

        assertNotNull(result);
        assertTrue(result > 0);
        // Expected: (3.5 * 1.0 * 3) + (10 * 0.3) + (50 * 0.1) = 10.5 + 3 + 5 = 18.5
        assertEquals(18.5, result, 0.01);
    }

    @Test
    void calculateBurnedCalories_ForCardioExercise_ShouldReturnCorrectValue() {
        Exercise exercise = Exercise.builder()
                .id(UUID.randomUUID())
                .type(ExerciseType.CARDIO)
                .muscleGroupTarget(List.of(MuscleTarget.builder().intensity(Intensity.HIGH).build()))
                .build();

        CreateWorkoutExerciseDTO dto = CreateWorkoutExerciseDTO.builder()
                .duration(30)
                .build();

        Double result = FitnessMath.calculateBurnedCalories(exercise, dto);

        assertNotNull(result);
        assertTrue(result > 0);
        // Expected: (5.0 * 1.5 * (30 / 60.0)) = 3.75
        assertEquals(3.75, result, 0.01);
    }

    @Test
    void calculateBurnedCalories_WithNullDuration_ShouldUseDefault() {
        Exercise exercise = Exercise.builder()
                .id(UUID.randomUUID())
                .type(ExerciseType.CARDIO)
                .muscleGroupTarget(List.of(MuscleTarget.builder().intensity(Intensity.LOW).build()))
                .build();

        CreateWorkoutExerciseDTO dto = CreateWorkoutExerciseDTO.builder()
                .build();

        Double result = FitnessMath.calculateBurnedCalories(exercise, dto);

        assertNotNull(result);
        assertTrue(result > 0);
        // Expected: (5.0 * 0.5 * 3) = 7.5
        assertEquals(7.5, result, 0.01);
    }

    @Test
    void calculateBurnedCalories_ForPlyometricExercise_ShouldUseCorrectMultiplier() {
        Exercise exercise = Exercise.builder()
                .id(UUID.randomUUID())
                .type(ExerciseType.PLYOMETRIC)
                .muscleGroupTarget(List.of(MuscleTarget.builder().intensity(Intensity.MODERATE).build()))
                .build();

        CreateWorkoutExerciseDTO dto = CreateWorkoutExerciseDTO.builder()
                .duration(60)
                .build();

        Double result = FitnessMath.calculateBurnedCalories(exercise, dto);

        assertNotNull(result);
        assertTrue(result > 0);
        // Expected: (3.5 * 1.0 * (60 / 60.0)) = 3.5
        assertEquals(3.5, result, 0.01);
    }

    @Test
    void calculateBurnedCalories_ForFlexibilityExercise_ShouldUseLowMultiplier() {
        Exercise exercise = Exercise.builder()
                .id(UUID.randomUUID())
                .type(ExerciseType.FLEXIBILITY)
                .muscleGroupTarget(List.of(MuscleTarget.builder().intensity(Intensity.LOW).build()))
                .build();

        CreateWorkoutExerciseDTO dto = CreateWorkoutExerciseDTO.builder()
                .duration(120)
                .build();

        Double result = FitnessMath.calculateBurnedCalories(exercise, dto);

        assertNotNull(result);
        assertTrue(result > 0);
        // Expected: (3.5 * 0.5 * (120 / 60.0)) = 3.5
        assertEquals(3.5, result, 0.01);
    }

    @Test
    void calculateBurnedCalories_ForBalanceExercise_ShouldUseLowMultiplier() {
        Exercise exercise = Exercise.builder()
                .id(UUID.randomUUID())
                .type(ExerciseType.BALANCE)
                .muscleGroupTarget(List.of(MuscleTarget.builder().intensity(Intensity.LOW).build()))
                .build();

        CreateWorkoutExerciseDTO dto = CreateWorkoutExerciseDTO.builder()
                .duration(90)
                .build();

        Double result = FitnessMath.calculateBurnedCalories(exercise, dto);

        assertNotNull(result);
        assertTrue(result > 0);
        // Expected: (3.5 * 0.5 * (90 / 60.0)) = 2.625
        assertEquals(2.625, result, 0.01);
    }

    @Test
    void calculateBurnedCalories_WithHighIntensity_ShouldUseHighMultiplier() {
        Exercise exercise = Exercise.builder()
                .id(UUID.randomUUID())
                .type(ExerciseType.STRENGTH)
                .muscleGroupTarget(List.of(MuscleTarget.builder().intensity(Intensity.HIGH).build()))
                .build();

        CreateWorkoutExerciseDTO dto = CreateWorkoutExerciseDTO.builder()
                .reps(5)
                .build();

        Double result = FitnessMath.calculateBurnedCalories(exercise, dto);

        assertNotNull(result);
        assertTrue(result > 0);
        // Expected: (3.5 * 1.5 * 3) + (5 * 0.3) = 15.75 + 1.5 = 17.25
        assertEquals(17.25, result, 0.01);
    }

    @Test
    void calculateBurnedCalories_WithNullExercise_ShouldReturnNull() {
        CreateWorkoutExerciseDTO dto = CreateWorkoutExerciseDTO.builder()
                .reps(10)
                .weight(50.0)
                .build();

        Double result = FitnessMath.calculateBurnedCalories(null, dto);

        assertNull(result);
    }

    @Test
    void calculateBurnedCalories_WithNullDto_ShouldReturnNull() {
        Exercise exercise = Exercise.builder()
                .id(UUID.randomUUID())
                .type(ExerciseType.STRENGTH)
                .muscleGroupTarget(List.of(MuscleTarget.builder().intensity(Intensity.MODERATE).build()))
                .build();

        Double result = FitnessMath.calculateBurnedCalories(exercise, null);

        assertNull(result);
    }

    @Test
    void calculateBurnedCalories_WithEmptyMuscleTargets_ShouldUseDefaultMultiplier() {
        Exercise exercise = Exercise.builder()
                .id(UUID.randomUUID())
                .type(ExerciseType.STRENGTH)
                .muscleGroupTarget(List.of())
                .build();

        CreateWorkoutExerciseDTO dto = CreateWorkoutExerciseDTO.builder()
                .reps(10)
                .weight(50.0)
                .build();

        Double result = FitnessMath.calculateBurnedCalories(exercise, dto);

        assertNotNull(result);
        // Expected: (3.5 * 1.0 * 3) + (10 * 0.3) + (50 * 0.1) = 10.5 + 3 + 5 = 18.5 (default multiplier)
        assertEquals(18.5, result, 0.01);
    }
}
