package com.momentum.fitness.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateWorkoutRequest {
    private String type;
    private String duration;
    private String fitnessLevel;
    private String goals;
    private String userId;
}


