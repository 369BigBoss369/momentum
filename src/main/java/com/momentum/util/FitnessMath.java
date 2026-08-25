package com.momentum.util;

import com.momentum.fitness.dto.CreateWorkoutExerciseDTO;
import com.momentum.fitness.model.*;
import com.momentum.fitness.model.enums.Intensity;

import java.util.Comparator;

public class FitnessMath {
    public static Double calculateBurnedCalories(Exercise exercise, CreateWorkoutExerciseDTO dto) {
        if (exercise == null || dto == null) {
            return null;
        }
        Intensity intensity = getExerciseIntensity(exercise);
        double baseCaloriesPerMinute = exercise.getType().getBaseMet();
        double caloriesPerMinute = baseCaloriesPerMinute * intensity.getMet();

        double total = 0;

        switch (exercise.getType()) {
            case STRENGTH -> {
                if (dto.getReps() != null) {
                    total += dto.getReps() * 0.3;
                }
                if (dto.getWeight() != null) {
                    total += dto.getWeight() * 0.1;
                }

                total += caloriesPerMinute * 3;
            }

            case PLYOMETRIC -> {
                if (dto.getReps() != null) {
                    total += dto.getReps() * 0.3;
                }

                if (dto.getDuration() != null && dto.getDuration() > 0) {
                    total += caloriesPerMinute * (dto.getDuration() / 60.0);
                } else {
                    total += caloriesPerMinute * 3;
                }
            }

            case CARDIO, AGILITY, BALANCE, FLEXIBILITY, RECOVERY -> {
                if (dto.getDuration() != null && dto.getDuration() > 0) {
                    total += caloriesPerMinute * (dto.getDuration() / 60.0);
                } else {
                    total += caloriesPerMinute * 3;
                }
            }
        }

        return total;
    }

    private static Intensity getExerciseIntensity(Exercise exercise) {
        return exercise.getMuscleGroupTarget().stream()
                .map(MuscleTarget::getIntensity)
                .max(Comparator.comparingDouble(Intensity::getMet))
                .orElse(Intensity.MODERATE);
    }

    public static long estimateDurationSeconds(WorkoutExercise workoutExercise) {
        if (workoutExercise == null) {
            return 0;
        }
        if (workoutExercise.getDuration() != null && workoutExercise.getDuration() > 0) {
            return workoutExercise.getDuration();
        }

        Integer reps = workoutExercise.getReps();
        Exercise exercise = workoutExercise.getExercise();
        var type = exercise != null ? exercise.getType() : null;

        if (type == com.momentum.fitness.model.enums.ExerciseType.PLYOMETRIC) {
            return reps != null ? (reps * 2L) + 60 : 180;
        }

        return reps != null ? (reps * 3L) + 60 : 180;
    }
}

