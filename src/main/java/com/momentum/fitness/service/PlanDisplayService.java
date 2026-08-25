package com.momentum.fitness.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.momentum.fitness.dto.PlanSummaryDTO;
import com.momentum.fitness.dto.TrackerDataDTO;
import com.momentum.fitness.model.Plan;
import com.momentum.fitness.model.PlanDay;
import com.momentum.fitness.model.Workout;
import com.momentum.user.service.UserService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class PlanDisplayService {

    private final PlanService planService;
    private final CompletionService completionService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Autowired
    public PlanDisplayService(PlanService planService, CompletionService completionService, UserService userService, ObjectMapper objectMapper) {
        this.planService = planService;
        this.completionService = completionService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    
    public PlanDisplayData preparePlanDisplayData(UUID userId) throws JsonProcessingException {
        log.debug("Preparing plan display data for user: {}", userId);

        TrackerDataDTO trackerData = planService.getTrackerDataForUser(userId);
        Plan activePlan = trackerData.getActivePlan();

        if (activePlan == null) {

            List<PlanSummaryDTO> planSummaries = planService.getPlanSummariesForUser(userId);
            return new PlanDisplayData(null, null, null, null, null, null, null,
                                     planSummaries, planSummaries.size(), 0, null);
        }


        Plan plan = planService.getAccessibleByIdWithPlanDays(activePlan.getId(), userService.getById(userId));


        Map<UUID, Boolean> planDayCompletionStatus = new HashMap<>();
        for (PlanDay planDay : plan.getPlanDays()) {
            boolean isCompleted = completionService.isPlanDayCompleted(userId, planDay);
            planDayCompletionStatus.put(planDay.getId(), isCompleted);
        }


        int totalWorkouts = plan.getPlanDays().stream()
                .mapToInt(day -> day.getWorkouts() != null ? day.getWorkouts().size() : 0)
                .sum();


        LocalDate planStartDate = userService.getCurrentPlanStartDate(userId);


        AdaptivePlanData adaptiveData = calculateAdaptivePlanData(plan, planDayCompletionStatus, planStartDate, userId);


        String planDaysJson = objectMapper.writeValueAsString(adaptiveData.getSimplifiedPlanDays());
        String completionStatusJson = objectMapper.writeValueAsString(planDayCompletionStatus);

        log.debug("Prepared plan display data with {} plan days and {} total workouts",
                 adaptiveData.getSimplifiedPlanDays().size(), totalWorkouts);

        return new PlanDisplayData(plan, plan.getPlanDays(), planDayCompletionStatus,
                                 trackerData.getCurrentDay(), trackerData.getPlanDaysCount(),
                                 totalWorkouts, adaptiveData.getSimplifiedPlanDays(),
                                 null, 0, 0, planStartDate, planDaysJson, completionStatusJson);
    }

    
    private AdaptivePlanData calculateAdaptivePlanData(Plan plan, Map<UUID, Boolean> planDayCompletionStatus,
                                                      LocalDate planStartDate, UUID userId) {
        List<PlanDay> planDays = plan.getPlanDays();
        Set<Integer> skippedDayNumbers = new HashSet<>();
        List<Map<String, Object>> simplifiedPlanDays = new ArrayList<>();
        int effectiveDayCounter = 1;


        for (PlanDay planDay : planDays) {
            int originalDayNumber = planDay.getDayNumber();
            LocalDate planDayDate = planStartDate.plusDays(originalDayNumber - 1);
            LocalDate today = LocalDate.now();


            if (planDayDate.isBefore(today) && !planDayCompletionStatus.get(planDay.getId())) {
                skippedDayNumbers.add(originalDayNumber);
            }
        }


        for (PlanDay planDay : planDays) {
            int originalDayNumber = planDay.getDayNumber();
            boolean isSkipped = skippedDayNumbers.contains(originalDayNumber);


            CompletionCounts counts = calculateCompletionCounts(planDay, userId, planDayCompletionStatus.keySet().iterator().next());


            Map<String, Object> dayData = new HashMap<>();
            dayData.put("id", planDay.getId().toString());
            dayData.put("dayNumber", originalDayNumber);
            dayData.put("originalDayNumber", originalDayNumber);
            dayData.put("effectiveDayNumber", effectiveDayCounter);
            dayData.put("type", planDay.getType().name());
            dayData.put("workoutCount", planDay.getWorkouts() != null ? planDay.getWorkouts().size() : 0);
            dayData.put("completedWorkoutsCount", counts.completedWorkouts);
            dayData.put("totalExercisesCount", counts.totalExercises);
            dayData.put("completedExercisesCount", counts.completedExercises);
            dayData.put("isSkipped", isSkipped);
            dayData.put("isCompleted", planDayCompletionStatus.get(planDay.getId()));

            simplifiedPlanDays.add(dayData);

            if (!isSkipped) {
                effectiveDayCounter++;
            }
        }

        return new AdaptivePlanData(simplifiedPlanDays, skippedDayNumbers);
    }

    
    private CompletionCounts calculateCompletionCounts(PlanDay planDay, UUID userId, UUID planDayId) {
        int completedWorkoutsCount = 0;
        int completedExercisesCount = 0;
        int totalExercisesCount = 0;

        if (planDay.getWorkouts() != null) {
            for (int i = 0; i < planDay.getWorkouts().size(); i++) {
                Workout workout = planDay.getWorkouts().get(i);
                boolean workoutCompleted = completionService.isWorkoutCompleted(userId, workout.getId(), planDayId, i);

                if (workoutCompleted) {
                    completedWorkoutsCount++;
                }


                if (workout.getWorkoutExercises() != null) {
                    totalExercisesCount += workout.getWorkoutExercises().size();


                    for (int j = 0; j < workout.getWorkoutExercises().size(); j++) {
                        var exercise = workout.getWorkoutExercises().get(j);
                        boolean exerciseCompleted = completionService.isExerciseCompleted(userId, exercise.getId(), planDayId, i);
                        if (exerciseCompleted) {
                            completedExercisesCount++;
                        }
                    }
                }
            }
        }

        return new CompletionCounts(completedWorkoutsCount, totalExercisesCount, completedExercisesCount);
    }

    
    @Getter
    public static class PlanDisplayData {
        private final Plan activePlan;
        private final List<PlanDay> planDays;
        private final Map<UUID, Boolean> planDayCompletionStatus;
        private final Integer currentDay;
        private final Integer totalDays;
        private final Integer totalWorkouts;
        private final List<Map<String, Object>> simplifiedPlanDays;
        private final List<PlanSummaryDTO> planSummaries;
        private final Integer planCount;
        private final Integer totalWorkoutsInSummaries;
        private final LocalDate planStartDate;
        private final String planDaysJson;
        private final String completionStatusJson;

        public PlanDisplayData(Plan activePlan, List<PlanDay> planDays, Map<UUID, Boolean> planDayCompletionStatus,
                             Integer currentDay, Integer totalDays, Integer totalWorkouts,
                             List<Map<String, Object>> simplifiedPlanDays, List<PlanSummaryDTO> planSummaries,
                             Integer planCount, Integer totalWorkoutsInSummaries, LocalDate planStartDate) {
            this(activePlan, planDays, planDayCompletionStatus, currentDay, totalDays, totalWorkouts,
                 simplifiedPlanDays, planSummaries, planCount, totalWorkoutsInSummaries, planStartDate, null, null);
        }

        public PlanDisplayData(Plan activePlan, List<PlanDay> planDays, Map<UUID, Boolean> planDayCompletionStatus,
                             Integer currentDay, Integer totalDays, Integer totalWorkouts,
                             List<Map<String, Object>> simplifiedPlanDays, List<PlanSummaryDTO> planSummaries,
                             Integer planCount, Integer totalWorkoutsInSummaries, LocalDate planStartDate,
                             String planDaysJson, String completionStatusJson) {
            this.activePlan = activePlan;
            this.planDays = planDays;
            this.planDayCompletionStatus = planDayCompletionStatus;
            this.currentDay = currentDay;
            this.totalDays = totalDays;
            this.totalWorkouts = totalWorkouts;
            this.simplifiedPlanDays = simplifiedPlanDays;
            this.planSummaries = planSummaries;
            this.planCount = planCount;
            this.totalWorkoutsInSummaries = totalWorkoutsInSummaries;
            this.planStartDate = planStartDate;
            this.planDaysJson = planDaysJson;
            this.completionStatusJson = completionStatusJson;
        }


    }

    
    @Getter
    private static class AdaptivePlanData {
        private final List<Map<String, Object>> simplifiedPlanDays;
        private final Set<Integer> skippedDayNumbers;

        public AdaptivePlanData(List<Map<String, Object>> simplifiedPlanDays, Set<Integer> skippedDayNumbers) {
            this.simplifiedPlanDays = simplifiedPlanDays;
            this.skippedDayNumbers = skippedDayNumbers;
        }

    }


    private static class CompletionCounts {
        private final int completedWorkouts;
        private final int totalExercises;
        private final int completedExercises;

        public CompletionCounts(int completedWorkouts, int totalExercises, int completedExercises) {
            this.completedWorkouts = completedWorkouts;
            this.totalExercises = totalExercises;
            this.completedExercises = completedExercises;
        }
    }
}

