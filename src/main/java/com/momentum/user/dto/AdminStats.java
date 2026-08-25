package com.momentum.user.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AdminStats {
    private long totalUsers;
    private long totalAdmins;
    private long totalProducts;
    private long totalCompositeFoods;
    private long totalRecipes;
    private long totalExercises;
    private long totalWorkouts;
    private long totalPlans;
    private long totalCompletions;
}