package com.momentum.fitness.service;

import com.momentum.fitness.dto.TrackerDataDTO;
import com.momentum.fitness.model.Plan;
import com.momentum.fitness.model.PlanDay;
import com.momentum.fitness.model.Workout;
import com.momentum.fitness.model.enums.PlanDayType;
import com.momentum.user.model.User;
import com.momentum.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class TrackerService {

    private final PlanService planService;
    private final CompletionService completionService;
    private final UserService userService;

    @Autowired
    public TrackerService(PlanService planService, CompletionService completionService, UserService userService) {
        this.planService = planService;
        this.completionService = completionService;
        this.userService = userService;
    }

    
    public TrackerDisplayData prepareTrackerData(UUID userId) {
        log.debug("Preparing tracker data for user: {}", userId);

        TrackerDataDTO trackerData = planService.getTrackerDataForUser(userId);
        Plan activePlan = trackerData.getActivePlan();


        if (activePlan != null) {
            autoCompleteRestDays(userId, activePlan);
        }


        PlanDay currentPlanDay = trackerData.getCurrentPlanDay();
        boolean currentPlanDayCompleted = currentPlanDay != null &&
                completionService.isPlanDayCompleted(userId, currentPlanDay);


        TrackerCompletionStatus completionStatus = null;
        if (currentPlanDay != null && currentPlanDay.getWorkouts() != null) {
            completionStatus = buildCompletionStatus(userId, currentPlanDay);
        }

        return new TrackerDisplayData(
            trackerData,
            currentPlanDayCompleted,
            completionStatus
        );
    }

    
    private void autoCompleteRestDays(UUID userId, Plan activePlan) {
        LocalDate planStartDate = userService.getCurrentPlanStartDate(userId);
        LocalDate today = LocalDate.now();
        long daysSinceStart = ChronoUnit.DAYS.between(planStartDate, today) + 1;

        Plan plan = planService.getAccessibleByIdWithPlanDays(activePlan.getId(), userService.getById(userId));
        if (daysSinceStart >= 1 && daysSinceStart <= plan.getPlanDays().size()) {
            PlanDay todaysPlanDay = plan.getPlanDays().get((int)daysSinceStart - 1);

            if (todaysPlanDay.getType() == PlanDayType.REST &&
                !completionService.isPlanDayCompleted(userId, todaysPlanDay.getId())) {

                User user = userService.getById(userId);
                log.debug("Auto-completing rest day {} for user {}", todaysPlanDay.getId(), userId);
                completionService.markPlanDayAsCompleted(user, todaysPlanDay.getId());
            }
        }
    }

    
    private TrackerCompletionStatus buildCompletionStatus(UUID userId, PlanDay planDay) {
        List<Workout> currentDayWorkouts = planDay.getWorkouts() != null ?
            new ArrayList<>(planDay.getWorkouts()) : new ArrayList<>();

        Map<String, Boolean> exerciseCompletionStatus = new HashMap<>();
        Map<String, Boolean> workoutCompletionStatus = new HashMap<>();

        for (int workoutIndex = 0; workoutIndex < currentDayWorkouts.size(); workoutIndex++) {
            Workout workout = currentDayWorkouts.get(workoutIndex);
            log.debug("Processing workout {} at index {}", workout.getId(), workoutIndex);

            boolean workoutCompleted = completionService.isWorkoutCompleted(userId, workout.getId(), planDay.getId(), workoutIndex);
            workoutCompletionStatus.put(workout.getId() + "_" + workoutIndex, workoutCompleted);

            log.debug("Workout {} at position {} completed: {}", workout.getId(), workoutIndex, workoutCompleted);

            if (workout.getWorkoutExercises() != null) {
                for (int exerciseIndex = 0; exerciseIndex < workout.getWorkoutExercises().size(); exerciseIndex++) {
                    var exercise = workout.getWorkoutExercises().get(exerciseIndex);
                    boolean exerciseCompleted = completionService.isExerciseCompleted(userId, exercise.getId(), planDay.getId(), workoutIndex);
                    exerciseCompletionStatus.put(exercise.getId() + "_" + workoutIndex, exerciseCompleted);
                    log.debug("Exercise {} at workout position {} completed: {}", exercise.getId(), workoutIndex, exerciseCompleted);
                }
            }
        }

        return new TrackerCompletionStatus(currentDayWorkouts, exerciseCompletionStatus, workoutCompletionStatus);
    }

    
    public static class TrackerDisplayData {
        private final TrackerDataDTO trackerData;
        private final boolean currentPlanDayCompleted;
        private final TrackerCompletionStatus completionStatus;

        public TrackerDisplayData(TrackerDataDTO trackerData, boolean currentPlanDayCompleted, TrackerCompletionStatus completionStatus) {
            this.trackerData = trackerData;
            this.currentPlanDayCompleted = currentPlanDayCompleted;
            this.completionStatus = completionStatus;
        }

        public TrackerDataDTO getTrackerData() { return trackerData; }
        public boolean isCurrentPlanDayCompleted() { return currentPlanDayCompleted; }
        public TrackerCompletionStatus getCompletionStatus() { return completionStatus; }
    }

    
    public static class TrackerCompletionStatus {
        private final List<Workout> currentDayWorkouts;
        private final Map<String, Boolean> exerciseCompletionStatus;
        private final Map<String, Boolean> workoutCompletionStatus;

        public TrackerCompletionStatus(List<Workout> currentDayWorkouts,
                                     Map<String, Boolean> exerciseCompletionStatus,
                                     Map<String, Boolean> workoutCompletionStatus) {
            this.currentDayWorkouts = currentDayWorkouts;
            this.exerciseCompletionStatus = exerciseCompletionStatus;
            this.workoutCompletionStatus = workoutCompletionStatus;
        }

        public List<Workout> getCurrentDayWorkouts() { return currentDayWorkouts; }
        public Map<String, Boolean> getExerciseCompletionStatus() { return exerciseCompletionStatus; }
        public Map<String, Boolean> getWorkoutCompletionStatus() { return workoutCompletionStatus; }
    }
}

