package com.momentum.fitness.dto;

import com.momentum.fitness.model.WorkoutExercise;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class WorkoutExerciseDTO {
    private int number;
    private Integer reps;
    private Double weight;
    private Integer duration;
    private ExerciseDTO exercise;

    public static WorkoutExerciseDTO from(WorkoutExercise workoutExercise) {
        return WorkoutExerciseDTO.builder()
                .number(workoutExercise.getNumber())
                .reps(workoutExercise.getReps())
                .weight(workoutExercise.getWeight())
                .duration(workoutExercise.getDuration())
                .exercise(workoutExercise.getExercise() != null ? ExerciseDTO.from(workoutExercise.getExercise()) : null)
                .build();
    }
}
























