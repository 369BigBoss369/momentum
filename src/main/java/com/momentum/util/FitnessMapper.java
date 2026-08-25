package com.momentum.util;

import com.momentum.fitness.dto.ExerciseSearchDTO;
import com.momentum.fitness.dto.WorkoutSearchDTO;
import com.momentum.fitness.model.Exercise;
import com.momentum.fitness.model.Workout;

import java.util.List;
import java.util.stream.Collectors;

public class FitnessMapper {
    public static List<ExerciseSearchDTO> mapToExerciseSearchDTO(List<Exercise> exercises) {
        return exercises.stream()
                .map(ExerciseSearchDTO::from)
                .collect(Collectors.toList());
    }

    public static List<WorkoutSearchDTO> mapToWorkoutSearchDTO(List<Workout> workouts) {
        return workouts.stream()
                .map(WorkoutSearchDTO::from)
                .collect(Collectors.toList());
    }
}

