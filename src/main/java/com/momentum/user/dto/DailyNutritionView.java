package com.momentum.user.dto;

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

public class DailyNutritionView {
    private int caloriesConsumed;
    private int caloriesGoal;

    private int carbsConsumed;
    private int carbsGoal;

    private int proteinConsumed;
    private int proteinGoal;

    private int fatConsumed;
    private int fatGoal;

    private int sugarConsumed;
    private int sugarGoal;
    private int fiberConsumed;
    private int fiberGoal;
    private int saturatedFatConsumed;
    private int saturatedFatGoal;
    private int monoUnsaturatedConsumed;
    private int monoUnsaturatedGoal;
    private int polyUnsaturatedConsumed;
    private int polyUnsaturatedGoal;
    private int transFatConsumed;
    private int transFatGoal;
    private int ironGoal;
    private int sodiumConsumed;
    private int sodiumGoal;
    private int potassiumConsumed;
    private int potassiumGoal;
    private int calciumConsumed;
    private int calciumGoal;
    private int cholesterolConsumed;
    private int cholesterolGoal;
    private int caffeineConsumed;
    private int caffeineGoal;
    private int alcoholConsumed;
    private int alcoholGoal;
    private int glycemicIndex;
    private int glycemicLoad;
}


