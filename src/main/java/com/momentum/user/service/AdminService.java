package com.momentum.user.service;

import com.momentum.exception.user.UserNotFoundException;
import com.momentum.fitness.model.Exercise;
import com.momentum.fitness.model.Plan;
import com.momentum.fitness.model.Workout;
import com.momentum.fitness.service.CompletionService;
import com.momentum.fitness.service.ExerciseService;
import com.momentum.fitness.service.PlanService;
import com.momentum.fitness.service.WorkoutService;
import com.momentum.nutrition.model.CompositeFood;
import com.momentum.nutrition.model.Product;
import com.momentum.nutrition.model.Recipe;
import com.momentum.nutrition.service.CompositeFoodService;
import com.momentum.nutrition.service.ProductService;
import com.momentum.nutrition.service.RecipeService;
import com.momentum.user.dto.AdminStats;
import com.momentum.user.dto.ModerationQueue;
import com.momentum.user.model.User;
import com.momentum.user.model.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class AdminService {

    private final UserService userService;
    private final ProductService productService;
    private final CompositeFoodService compositeFoodService;
    private final RecipeService recipeService;
    private final ExerciseService exerciseService;
    private final WorkoutService workoutService;
    private final PlanService planService;
    private final CompletionService completionService;

    @Autowired
    public AdminService(UserService userService, ProductService productService,
                        CompositeFoodService compositeFoodService, RecipeService recipeService,
                        ExerciseService exerciseService, WorkoutService workoutService,
                        PlanService planService, CompletionService completionService) {
        this.userService = userService;
        this.productService = productService;
        this.compositeFoodService = compositeFoodService;
        this.recipeService = recipeService;
        this.exerciseService = exerciseService;
        this.workoutService = workoutService;
        this.planService = planService;
        this.completionService = completionService;
    }

    public void updateUserRole(UUID userId, UserRole role, RedirectAttributes redirectAttributes) {
        log.info("Updating user role for userId: {} to role: {}", userId, role);

        try {
            userService.updateUserRole(userId, role);
            redirectAttributes.addFlashAttribute("successMessage",
                "User role updated successfully to " + role);
            log.info("Successfully updated user role for userId: {} to {}", userId, role);
        } catch (UserNotFoundException e) {
            log.error("User not found when updating role for userId: {}", userId, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                "User not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to update user role for userId: {}", userId, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                "Failed to update user role: " + e.getMessage());
        }
    }

    public void toggleUserEnabled(UUID userId, boolean enabled, UUID actingAdminId, RedirectAttributes redirectAttributes) {
        try {
            if (userId.equals(actingAdminId) && !enabled) {
                redirectAttributes.addFlashAttribute("errorMessage", "You cannot deactivate your own account.");
                return;
            }

            User targetUser = userService.getById(userId);
            if (targetUser.getRole() == UserRole.ADMIN && !enabled && userService.countActiveAdmins() <= 1) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cannot deactivate the last active admin account.");
                return;
            }

            userService.setEnabled(userId, enabled);
            redirectAttributes.addFlashAttribute("successMessage",
                    "User account " + (enabled ? "reactivated" : "deactivated") + " successfully");
        } catch (Exception e) {
            log.error("Failed to update enabled status for userId: {}", userId, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to update account status: " + e.getMessage());
        }
    }

    public AdminStats getStats() {
        return AdminStats.builder()
                .totalUsers(userService.countByRole(UserRole.USER))
                .totalAdmins(userService.countByRole(UserRole.ADMIN))
                .totalProducts(productService.count())
                .totalCompositeFoods(compositeFoodService.count())
                .totalRecipes(recipeService.count())
                .totalExercises(exerciseService.count())
                .totalWorkouts(workoutService.count())
                .totalPlans(planService.count())
                .totalCompletions(completionService.count())
                .build();
    }

    public ModerationQueue getModerationQueue() {
        List<Product> pendingProducts = productService.getPendingApproval();
        List<CompositeFood> pendingCompositeFoods = compositeFoodService.getPendingApproval();
        List<Recipe> pendingRecipes = recipeService.getPendingApproval();
        List<Exercise> pendingExercises = exerciseService.getPendingApproval();
        List<Workout> pendingWorkouts = workoutService.getPendingApproval();
        List<Plan> pendingPlans = planService.getPendingApproval();

        Set<UUID> ownerIds = new HashSet<>();
        pendingProducts.forEach(p -> ownerIds.add(p.getOwnerId()));
        pendingCompositeFoods.forEach(p -> ownerIds.add(p.getOwnerId()));
        pendingRecipes.forEach(p -> ownerIds.add(p.getOwnerId()));
        pendingExercises.forEach(p -> ownerIds.add(p.getOwnerId()));
        pendingWorkouts.forEach(p -> ownerIds.add(p.getOwnerId()));
        pendingPlans.forEach(p -> ownerIds.add(p.getOwnerId()));

        return ModerationQueue.builder()
                .pendingProducts(pendingProducts)
                .pendingCompositeFoods(pendingCompositeFoods)
                .pendingRecipes(pendingRecipes)
                .pendingExercises(pendingExercises)
                .pendingWorkouts(pendingWorkouts)
                .pendingPlans(pendingPlans)
                .usernamesByOwnerId(userService.getUsernamesByIds(ownerIds))
                .build();
    }

    public void approveItem(String type, UUID id, RedirectAttributes redirectAttributes) {
        try {
            switch (type) {
                case "product" -> productService.approve(id);
                case "composite" -> compositeFoodService.approve(id);
                case "recipe" -> recipeService.approve(id);
                case "exercise" -> exerciseService.approve(id);
                case "workout" -> workoutService.approve(id);
                case "plan" -> planService.approve(id);
                default -> throw new IllegalArgumentException("Unknown content type: " + type);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Item approved successfully");
        } catch (Exception e) {
            log.error("Failed to approve item type={} id={}", type, id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to approve item: " + e.getMessage());
        }
    }

    public void rejectItem(String type, UUID id, RedirectAttributes redirectAttributes) {
        try {
            switch (type) {
                case "product" -> productService.reject(id);
                case "composite" -> compositeFoodService.reject(id);
                case "recipe" -> recipeService.reject(id);
                case "exercise" -> exerciseService.reject(id);
                case "workout" -> workoutService.reject(id);
                case "plan" -> planService.reject(id);
                default -> throw new IllegalArgumentException("Unknown content type: " + type);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Item rejected");
        } catch (Exception e) {
            log.error("Failed to reject item type={} id={}", type, id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to reject item: " + e.getMessage());
        }
    }
}

