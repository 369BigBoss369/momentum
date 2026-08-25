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
public class GeneratePlanResponse {
    private String name;
    private String description;
    private String type;
    private List<AIGeneratedPlanDay> planDays;
    private boolean success;
    private String errorMessage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIGeneratedPlanDay {
        private Integer dayNumber;
        private String type;
        private List<String> workoutNames;
    }
}

