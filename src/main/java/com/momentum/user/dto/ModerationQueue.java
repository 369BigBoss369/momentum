package com.momentum.user.dto;

import com.momentum.fitness.model.Exercise;
import com.momentum.fitness.model.Plan;
import com.momentum.fitness.model.Workout;
import com.momentum.nutrition.model.CompositeFood;
import com.momentum.nutrition.model.Product;
import com.momentum.nutrition.model.Recipe;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
@Getter
public class ModerationQueue {
    private List<Product> pendingProducts;
    private List<CompositeFood> pendingCompositeFoods;
    private List<Recipe> pendingRecipes;
    private List<Exercise> pendingExercises;
    private List<Workout> pendingWorkouts;
    private List<Plan> pendingPlans;
    private Map<UUID, String> usernamesByOwnerId;

    public int getTotalPending() {
        return pendingProducts.size() + pendingCompositeFoods.size() + pendingRecipes.size()
                + pendingExercises.size() + pendingWorkouts.size() + pendingPlans.size();
    }
}