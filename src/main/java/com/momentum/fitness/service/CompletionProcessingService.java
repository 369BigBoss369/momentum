package com.momentum.fitness.service;

import com.momentum.fitness.model.Completion;
import com.momentum.fitness.model.PlanDay;
import com.momentum.fitness.model.Workout;
import com.momentum.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class CompletionProcessingService {

    private final CompletionService completionService;
    private final PlanService planService;

    @Autowired
    public CompletionProcessingService(CompletionService completionService, PlanService planService) {
        this.completionService = completionService;
        this.planService = planService;
    }

    
    @Transactional
    public Completion processCompletionAndCascade(User user, String type, UUID targetId, UUID planDayId, Integer workoutPosition) {
        log.info("Processing completion - type: {}, targetId: {}, planDayId: {}, position: {} for user: {}",
                type, targetId, planDayId, workoutPosition, user.getId());


        Completion completion = completionService.markAsCompleted(user, targetId, type, planDayId, workoutPosition);


        if ("EXERCISE".equals(type.toUpperCase())) {
            handleExerciseCompletionCascade(user, targetId, planDayId, workoutPosition);
        }

        return completion;
    }

    
    private void handleExerciseCompletionCascade(User user, UUID exerciseId, UUID planDayId, Integer workoutPosition) {
        log.debug("Checking for workout completion cascade after exercise completion");


        var trackerData = planService.getTrackerDataForUser(user.getId());
        var currentPlanDay = trackerData.getCurrentPlanDay();

        if (currentPlanDay == null || currentPlanDay.getWorkouts() == null ||
            currentPlanDay.getWorkouts().size() <= workoutPosition) {
            log.debug("No valid plan day or workout position found for cascade check");
            return;
        }

        var workout = currentPlanDay.getWorkouts().get(workoutPosition);


        if (areAllExercisesCompletedInWorkout(user, workout, planDayId, workoutPosition)) {
            handleWorkoutCompletionCascade(user, workout, planDayId, workoutPosition, currentPlanDay);
        }
    }

    
    private boolean areAllExercisesCompletedInWorkout(User user, Workout workout, UUID planDayId, Integer workoutPosition) {
        if (workout.getWorkoutExercises() == null) {
            return true;
        }

        for (var exercise : workout.getWorkoutExercises()) {
            boolean isCompleted = completionService.isExerciseCompleted(user.getId(), exercise.getId(), planDayId, workoutPosition);
            if (!isCompleted) {
                return false;
            }
        }

        log.debug("All exercises completed in workout: {}", workout.getId());
        return true;
    }

    
    private void handleWorkoutCompletionCascade(User user, Workout workout, UUID planDayId, Integer workoutPosition, PlanDay currentPlanDay) {

        if (!completionService.isWorkoutCompleted(user.getId(), workout.getId(), planDayId, workoutPosition)) {
            log.debug("Marking workout as completed: {}", workout.getId());
            completionService.markWorkoutAsCompleted(user, workout.getId(), planDayId, workoutPosition);
        }


        if (areAllWorkoutsCompletedInPlanDay(user, currentPlanDay, planDayId)) {
            handlePlanDayCompletionCascade(user, planDayId);
        }
    }

    
    private boolean areAllWorkoutsCompletedInPlanDay(User user, PlanDay planDay, UUID planDayId) {
        if (planDay.getWorkouts() == null) {
            return true;
        }

        log.debug("Checking completion for {} workouts in plan day {}", planDay.getWorkouts().size(), planDayId);

        for (int i = 0; i < planDay.getWorkouts().size(); i++) {
            var workout = planDay.getWorkouts().get(i);
            boolean workoutCompleted = completionService.isWorkoutCompleted(user.getId(), workout.getId(), planDayId, i);
            log.debug("Workout {} at position {} completed: {}", workout.getId(), i, workoutCompleted);

            if (!workoutCompleted) {
                return false;
            }
        }

        log.debug("All workouts completed in plan day: {}", planDayId);
        return true;
    }

    
    private void handlePlanDayCompletionCascade(User user, UUID planDayId) {

        boolean planDayAlreadyCompleted = completionService.isPlanDayCompleted(user.getId(), planDayId);
        log.debug("Plan day already completed (checking for existing record): {}", planDayAlreadyCompleted);

        if (!planDayAlreadyCompleted) {
            log.debug("Creating plan day completion record for plan day: {}", planDayId);
            completionService.markPlanDayAsCompleted(user, planDayId);
            log.info("Plan day completion record created successfully for plan day: {}", planDayId);
        } else {
            log.debug("Plan day already completed, skipping record creation");
        }
    }

    
    @Transactional
    public void processWorkoutRestart(User user, UUID workoutId, UUID planDayId, Integer workoutPosition) {
        log.info("Processing workout restart - workoutId: {}, planDayId: {}, position: {} for user: {}",
                workoutId, planDayId, workoutPosition, user.getId());


        completionService.deleteCompletionsForWorkout(user.getId(), workoutId, planDayId, workoutPosition);


        var trackerData = planService.getTrackerDataForUser(user.getId());
        var currentPlanDay = trackerData.getCurrentPlanDay();

        if (currentPlanDay != null && currentPlanDay.getId().equals(planDayId)) {
            if (!areAllWorkoutsStillCompletedAfterRestart(user, currentPlanDay, planDayId)) {
                removePlanDayCompletionIfExists(user, planDayId);
            }
        }

        log.info("Workout restart processing completed for workout: {}", workoutId);
    }

    
    private boolean areAllWorkoutsStillCompletedAfterRestart(User user, PlanDay planDay, UUID planDayId) {
        if (planDay.getWorkouts() == null) {
            return true;
        }

        for (int i = 0; i < planDay.getWorkouts().size(); i++) {
            var workout = planDay.getWorkouts().get(i);
            if (!completionService.isWorkoutCompleted(user.getId(), workout.getId(), planDayId, i)) {
                return false;
            }
        }

        return true;
    }

    
    private void removePlanDayCompletionIfExists(User user, UUID planDayId) {
        try {
            if (completionService.isPlanDayCompleted(user.getId(), planDayId)) {
                log.debug("Removing plan day completion after workout restart for plan day: {}", planDayId);
                completionService.deletePlanDayCompletion(user.getId(), planDayId);
                log.info("Plan day completion removed successfully for plan day: {}", planDayId);
            } else {
                log.debug("No plan day completion found to remove");
            }
        } catch (Exception e) {
            log.debug("Plan day completion deletion failed: {}", e.getMessage());
        }
    }
}

