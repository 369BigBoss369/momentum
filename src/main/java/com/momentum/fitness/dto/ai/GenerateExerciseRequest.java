package com.momentum.fitness.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateExerciseRequest {
    private String muscleGroup;
    private String difficulty;
    private String equipment;
    private String userId;
}


