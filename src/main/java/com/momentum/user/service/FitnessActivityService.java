package com.momentum.user.service;

import com.momentum.user.dto.enums.FitnessActivityType;
import com.momentum.user.model.FitnessActivity;
import com.momentum.user.model.User;
import com.momentum.user.repository.FitnessActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FitnessActivityService {
    private final FitnessActivityRepository activityRepository;
    private final UserService userService;

    @Autowired
    public FitnessActivityService(FitnessActivityRepository activityRepository, UserService userService) {
        this.activityRepository = activityRepository;
        this.userService = userService;
    }

    public void logExerciseAddedToLibrary(UUID userId, String exerciseName) {
        saveActivity(userId, FitnessActivityType.EXERCISE_ADDED_TO_LIBRARY,
                String.format("Added exercise to library: %s", exerciseName));
    }

    public void logExerciseRemovedFromLibrary(UUID userId, String exerciseName) {
        saveActivity(userId, FitnessActivityType.EXERCISE_REMOVED_FROM_LIBRARY,
                String.format("Removed exercise from library: %s", exerciseName));
    }

    public void logWorkoutAddedToLibrary(UUID userId, String workoutName) {
        saveActivity(userId, FitnessActivityType.WORKOUT_ADDED_TO_LIBRARY,
                String.format("Added workout to library: %s", workoutName));
    }

    public void logWorkoutRemovedFromLibrary(UUID userId, String workoutName) {
        saveActivity(userId, FitnessActivityType.WORKOUT_REMOVED_FROM_LIBRARY,
                String.format("Removed workout from library: %s", workoutName));
    }

    public void logPlanAddedToLibrary(UUID userId, String planName) {
        saveActivity(userId, FitnessActivityType.PLAN_ADDED_TO_LIBRARY,
                String.format("Added plan to library: %s", planName));
    }

    public void logPlanRemovedFromLibrary(UUID userId, String planName) {
        saveActivity(userId, FitnessActivityType.PLAN_REMOVED_FROM_LIBRARY,
                String.format("Removed plan from library: %s", planName));
    }

    public void logPlanStarted(UUID userId, String planName) {
        saveActivity(userId, FitnessActivityType.PLAN_STARTED,
                String.format("Started following plan: %s", planName));
    }

    private void saveActivity(UUID userId, FitnessActivityType type, String message) {
        User user = userService.getById(userId);
        activityRepository.save(FitnessActivity.builder()
                .user(user)
                .type(type)
                .message(message)
                .build());
    }
}


