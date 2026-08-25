package com.momentum.user.service;

import com.momentum.user.dto.NutritionActivityPageResponse;
import com.momentum.user.dto.NutritionActivityView;
import com.momentum.user.model.NutritionActivity;
import com.momentum.user.model.User;
import com.momentum.user.model.enums.MealType;
import com.momentum.user.dto.enums.NutritionActivityType;
import com.momentum.user.repository.NutritionActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class NutritionActivityService {
    private final NutritionActivityRepository activityRepository;
    private final UserService userService;

    @Autowired
    public NutritionActivityService(NutritionActivityRepository activityRepository, UserService userService) {
        this.activityRepository = activityRepository;
        this.userService = userService;
    }

    @Transactional
    public void logMealAdded(UUID userId, MealType mealType, String foodName, int amount) {
        String mealLabel = formatMealType(mealType);
        String message = String.format("Added %dg %s to %s", amount, foodName, mealLabel);
        save(userId, NutritionActivityType.MEAL_ADDED, message);
    }

    @Transactional
    public void logMealRemoved(UUID userId, MealType mealType, String foodName, int amount) {
        String mealLabel = formatMealType(mealType);
        String message = String.format("Removed %dg %s from %s", amount, foodName, mealLabel);
        save(userId, NutritionActivityType.MEAL_REMOVED, message);
    }

    @Transactional
    public void logWaterLogged(UUID userId, double amount) {
        String message = String.format("Logged %.0f ml of water", amount);
        save(userId, NutritionActivityType.WATER_LOGGED, message);
    }

    @Transactional
    public void logFoodCreated(UUID userId, String foodName, String category) {
        String message = String.format("Created new %s: %s", category, foodName);
        save(userId, NutritionActivityType.FOOD_CREATED, message);
    }

    @Transactional
    public void logFoodUpdated(UUID userId, String foodName, String category) {
        String message = String.format("Updated %s: %s", category, foodName);
        save(userId, NutritionActivityType.FOOD_UPDATED, message);
    }
    @Transactional
    public void logFoodAddedToLibrary(UUID userId, String foodName, String category) {
        String message = String.format("Added %s to library: %s", category, foodName);
        save(userId, NutritionActivityType.FOOD_ADDED_TO_LIBRARY, message);
    }

    @Transactional
    public void logFoodRemovedFromLibrary(UUID userId, String foodName, String category) {
        String message = String.format("Removed %s from library: %s", category, foodName);
        save(userId, NutritionActivityType.FOOD_REMOVED_FROM_LIBRARY, message);
    }

    @Transactional
    public void logRecipeAddedToLibrary(UUID userId, String recipeName) {
        String message = String.format("Added recipe to library: %s", recipeName);
        save(userId, NutritionActivityType.RECIPE_ADDED_TO_LIBRARY, message);
    }

    @Transactional
    public void logRecipeRemovedFromLibrary(UUID userId, String recipeName) {
        String message = String.format("Removed recipe from library: %s", recipeName);
        save(userId, NutritionActivityType.RECIPE_REMOVED_FROM_LIBRARY, message);
    }

    @Transactional(readOnly = true)
    public NutritionActivityPageResponse getActivitiesPage(UUID userId, int page, int size) {
        int effectivePage = Math.max(0, page);
        int effectiveSize = (size <= 0 || size > 50) ? 10 : size;
        Pageable pageable = PageRequest.of(effectivePage, effectiveSize);

        Page<NutritionActivity> activityPage = activityRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);

        List<NutritionActivityView> content = activityPage.getContent()
                .stream()
                .map(activity -> NutritionActivityView.builder()
                        .id(activity.getId())
                        .type(activity.getType().name())
                        .message(activity.getMessage())
                        .createdAt(activity.getCreatedAt())
                        .build())
                .toList();

        return NutritionActivityPageResponse.builder()
                .content(content)
                .currentPage(activityPage.getNumber())
                .totalPages(activityPage.getTotalPages())
                .totalElements(activityPage.getTotalElements())
                .size(activityPage.getSize())
                .hasNext(activityPage.hasNext())
                .hasPrevious(activityPage.hasPrevious())
                .isFirst(activityPage.isFirst())
                .isLast(activityPage.isLast())
                .build();
    }

    private void save(UUID userId, NutritionActivityType type, String message) {
        User user = userService.getById(userId);
        NutritionActivity activity = NutritionActivity.builder()
                .user(user)
                .type(type)
                .message(message)
                .build();
        activityRepository.save(activity);
    }

    private String formatMealType(MealType mealType) {
        String name = mealType.name().toLowerCase(Locale.ROOT).replace("_", " ");
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}

