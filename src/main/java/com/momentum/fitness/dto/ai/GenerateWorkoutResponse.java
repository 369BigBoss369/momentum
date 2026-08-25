package com.momentum.fitness.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateWorkoutResponse {
    private String name;
    private String type;
    private List<AIGeneratedWorkoutExercise> workoutExercises;
    private boolean success;
    private String errorMessage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIGeneratedWorkoutExercise {
        private String exerciseName;
        private Integer number;
        private Integer reps;
        private Double weight;
        private Integer duration;
        private Double burnedCalories;
    }
}

