package com.momentum.nutrition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class DailyNutritionSummaryDTO {
    private int caloriesConsumed;
    private int caloriesGoal;

    private int carbsConsumed;
    private int carbsGoal;

    private int proteinConsumed;
    private int proteinGoal;

    private int fatConsumed;
    private int fatGoal;
}



















