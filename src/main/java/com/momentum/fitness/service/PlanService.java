package com.momentum.fitness.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.exception.UnauthorizedResourceAccessException;
import com.momentum.exception.fitness.ActivityNotFoundException;
import com.momentum.exception.fitness.CustomActivityAlreadyExists;
import com.momentum.fitness.dto.CreatePlanDTO;
import com.momentum.fitness.dto.CreatePlanDayDTO;
import com.momentum.fitness.dto.PlanDaySummaryDTO;
import com.momentum.fitness.dto.PlanSummaryDTO;
import com.momentum.fitness.dto.TrackerDataDTO;
import com.momentum.fitness.model.Plan;
import com.momentum.fitness.model.PlanDay;
import com.momentum.fitness.model.Workout;
import com.momentum.fitness.model.enums.PlanDayType;
import com.momentum.fitness.model.enums.PlanType;
import com.momentum.fitness.model.enums.SourceType;
import com.momentum.fitness.repository.PlanDayRepository;
import com.momentum.fitness.repository.PlanRepository;
import com.momentum.user.model.User;
import com.momentum.user.service.FitnessActivityService;
import com.momentum.user.service.UserService;
import com.momentum.util.AccessControlUtil;
import com.momentum.util.ModerationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PlanService {
    private final PlanRepository planRepository;
    private final PlanDayRepository planDayRepository;
    private final WorkoutService workoutService;
    private final FitnessActivityService fitnessActivityService;
    private final UserService userService;
    private final CompletionService completionService;

    @Autowired
    public PlanService(PlanRepository planRepository, PlanDayRepository planDayRepository, WorkoutService workoutService, FitnessActivityService fitnessActivityService, UserService userService, CompletionService completionService) {
        this.planRepository = planRepository;
        this.planDayRepository = planDayRepository;
        this.workoutService = workoutService;
        this.fitnessActivityService = fitnessActivityService;
        this.userService = userService;
        this.completionService = completionService;
    }

    @Transactional
    public Plan createPlan(CreatePlanDTO createPlanDto, UUID userId) {
        log.info("Creating plan: {} with {} days for user: {}", createPlanDto.getName(), createPlanDto.getDays().size(), userId);

        Optional<Plan> optional = planRepository.findByOwnerIdAndName(userId, createPlanDto.getName());
        if (optional.isPresent()) {
            throw new CustomActivityAlreadyExists(String.format("You have already added custom plan with the name '%s'", createPlanDto.getName()));
        }

        List<PlanDay> planDays = new ArrayList<>();
        for (CreatePlanDayDTO planDayDto : createPlanDto.getDays()) {
            log.debug("Creating plan day {} with {} workouts", planDayDto.getDayNumber(), planDayDto.getWorkoutIds().size());

            PlanDay planDay = PlanDay.builder()
                    .dayNumber(planDayDto.getDayNumber())
                    .type(planDayDto.getType())
                    .build();

            List<Workout> workouts = new ArrayList<>();
            if (!planDay.getType().equals(PlanDayType.REST)) {
                planDayDto.getWorkoutIds().forEach(id -> {
                    
                    workouts.add(workoutService.getAccessibleById(id, userService.getById(userId)));
                });
            }
            planDay.setWorkouts(workouts);
            

            planDays.add(planDay);
        }

        Plan plan = Plan.builder()
                .name(createPlanDto.getName())
                .description(createPlanDto.getDescription())
                .type(createPlanDto.getType())
                .build();

        plan.setIsPublic(createPlanDto.getIsPublic());
        ModerationUtil.applyPublicityChange(plan, false);
        plan.setOwnerId(userId);
        plan.setSource(SourceType.CUSTOM);

        for (PlanDay planDay : planDays) {
            planDay.setPlan(plan);
        }

        plan.setPlanDays(planDays);
        log.debug("Saving plan with {} plan days", planDays.size());
        Plan saved = planRepository.save(plan);
        log.info("Plan created successfully: {} (ID: {}) for user: {}", createPlanDto.getName(), plan.getId(), userId);
        return saved;
    }

    @Transactional
    public Plan updatePlan(UUID planId, CreatePlanDTO createPlanDto, UUID userId) {
        Plan existingPlan = planRepository.findById(planId).orElseThrow(() -> new ActivityNotFoundException("Plan not found"));
        Hibernate.initialize(existingPlan.getPlanDays());

        if (!existingPlan.getOwnerId().equals(userId)) {
            throw new UnauthorizedResourceAccessException("You don't have permission to edit this plan");
        }

        if (!existingPlan.getName().equals(createPlanDto.getName())) {
            Optional<Plan> optional = planRepository.findByOwnerIdAndName(userId, createPlanDto.getName());
            if (optional.isPresent()) {
                throw new CustomActivityAlreadyExists(String.format("You have already added custom plan with the name '%s'", createPlanDto.getName()));
            }
        }

        existingPlan.getPlanDays().clear();

        for (CreatePlanDayDTO planDayDto : createPlanDto.getDays()) {
            PlanDay planDay = PlanDay.builder()
                    .dayNumber(planDayDto.getDayNumber())
                    .type(planDayDto.getType())
                    .plan(existingPlan)
                    .build();

            List<Workout> workouts = new ArrayList<>();

            if (!planDay.getType().equals(PlanDayType.REST)) {
                for (int i = 0; i < planDayDto.getWorkoutIds().size(); i++) {
                    UUID workoutId = planDayDto.getWorkoutIds().get(i);

                    try {
                        Workout workout = workoutService.getAccessibleById(workoutId, userService.getById(userId));
                        workouts.add(workout);
                        
                    } catch (Exception e) {
                        System.err.println("Could not find workout " + workoutId + ": " + e.getMessage());

                    }
                }
            }

            planDay.setWorkouts(workouts);
            existingPlan.getPlanDays().add(planDay);
        }

        existingPlan.setName(createPlanDto.getName());
        existingPlan.setDescription(createPlanDto.getDescription());
        existingPlan.setType(createPlanDto.getType());

        boolean wasPublic = Boolean.TRUE.equals(existingPlan.getIsPublic());
        existingPlan.setIsPublic(createPlanDto.getIsPublic());
        ModerationUtil.applyPublicityChange(existingPlan, wasPublic);

        return planRepository.save(existingPlan);
    }

    @Transactional(readOnly = true)
    public Page<Plan> search(String query, PlanType planType, UUID userId, Pageable pageable) {
        return planRepository.search(query, planType, userId, pageable);
    }

    @Transactional(readOnly = true)
    public Plan getAccessibleById(UUID planId) {
        return planRepository.findByIdWithSharedUsers(planId).orElseThrow(() -> new ActivityNotFoundException("Plan not found"));
    }

    @Transactional(readOnly = true)
    public Plan getAccessibleByIdWithPlanDays(UUID planId, User currentUser) {
        Plan plan = planRepository.findByIdWithSharedUsers(planId).orElseThrow(() -> new ActivityNotFoundException("Plan not found"));

        if (!AccessControlUtil.canView(plan, currentUser)) {
            throw new ActivityNotFoundException("Plan does not exist");
        }

        plan.setPlanDays(planDayRepository.getByPlan_Id(planId));

        for (PlanDay planDay : plan.getPlanDays()) {
            List<Workout> workouts = new ArrayList<>();

            for (Workout workout : planDay.getWorkouts()) {
                Workout fullWorkout = workoutService.getFullWorkoutForNestedView(workout.getId());
                workouts.add(fullWorkout);
            }
            planDay.setWorkouts(workouts);
        }

        return plan;
    }

    public boolean isOwner(Plan plan, UUID userId) {
        return userId.equals(plan.getOwnerId());
    }

    @Transactional(readOnly = true)
    public List<PlanSummaryDTO> getPlanSummariesForUser(UUID userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return planRepository.findAllByOwnerId(userId).stream()
                .map(plan -> mapToPlanSummary(plan, userId))
                .collect(Collectors.toList());
    }

    private PlanSummaryDTO mapToPlanSummary(Plan plan, UUID userId) {
        List<PlanDay> planDays = plan.getPlanDays() != null ? plan.getPlanDays() : planDayRepository.getByPlan_Id(plan.getId());
        List<PlanDaySummaryDTO> daySummaries = planDays.stream()
                .sorted(Comparator.comparingInt(PlanDay::getDayNumber))
                .map(day -> {
                    List<Workout> workouts = day.getWorkouts() == null ? Collections.emptyList() : day.getWorkouts();
                    List<String> workoutNames = workouts.stream()
                            .map(Workout::getName)
                            .collect(Collectors.toList());
                    return PlanDaySummaryDTO.builder()
                            .dayNumber(day.getDayNumber())
                            .type(day.getType())
                            .workoutNames(workoutNames)
                            .build();
                })
                .collect(Collectors.toList());

        int totalWorkouts = daySummaries.stream()
                .mapToInt(day -> day.getWorkoutNames().size())
                .sum();

        return PlanSummaryDTO.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .type(plan.getType())
                .days(daySummaries)
                .totalWorkouts(totalWorkouts)
                .isInLibrary(isInLibrary(plan.getId(), userId))
                .build();
    }

    public Plan getById(UUID planId) {
        return planRepository.findById(planId).orElseThrow(() -> new ActivityNotFoundException("Plan not found"));
    }

    public boolean isInLibrary(UUID planId, UUID userId) {
        return planRepository.existsByIdAndSharedUsers_Id(planId, userId);
    }

    @Transactional
    public void addToLibrary(UUID planId, UUID userId) {
        Plan plan = planRepository.findByIdWithSharedUsers(planId).orElseThrow(() -> new ActivityNotFoundException("Plan not found"));

        if (!Boolean.TRUE.equals(plan.getIsPublic()) || !ModerationUtil.isVisible(plan)) {
            throw new IllegalStateException("This item is not yet approved and cannot be added to a library");
        }

        User user = userService.getById(userId);

        boolean alreadyShared = plan.getSharedUsers().stream().anyMatch(u -> u.getId().equals(userId));
        if (!alreadyShared) {
            plan.getSharedUsers().add(user);
            planRepository.save(plan);
            fitnessActivityService.logPlanAddedToLibrary(userId, plan.getName());
        }
    }

    @Transactional
    public void removeFromLibrary(UUID planId, UUID userId) {
        Plan plan = planRepository.findByIdWithSharedUsers(planId).orElseThrow(() -> new ActivityNotFoundException("Plan not found"));

        boolean removed = plan.getSharedUsers().removeIf(u -> u.getId().equals(userId));
        if (removed) {
            planRepository.save(plan);
            fitnessActivityService.logPlanRemovedFromLibrary(userId, plan.getName());
        }
    }

    @Transactional(readOnly = true)
    public TrackerDataDTO getTrackerDataForUser(UUID userId) {

        User user = userService.getByIdWithCurrentPlan(userId);
        Plan currentPlan = user.getCurrentPlan();

        if (currentPlan == null) {
            return new TrackerDataDTO(null, 0, 1, null);
        }


        Hibernate.initialize(currentPlan);


        int planDaysCount = (int) planDayRepository.countByPlanId(currentPlan.getId());


        int currentDay = 1;
        LocalDate planStartDate = userService.getCurrentPlanStartDate(userId);
        if (planStartDate != null) {
            LocalDate today = LocalDate.now();
            long daysDifference = java.time.temporal.ChronoUnit.DAYS.between(planStartDate, today);

            currentDay = (int) daysDifference + 1;


            if (currentDay > planDaysCount) {
                
                userService.clearCurrentPlan(userId);

                return new TrackerDataDTO(null, 0, 0, null);
            }


            if (currentDay < 1) {
                currentDay = 1;
            }
        }


        PlanDay currentPlanDay = null;
        if (currentDay <= planDaysCount) {
            List<PlanDay> currentDayList = planDayRepository.findByPlanIdAndDayNumber(currentPlan.getId(), currentDay);
            if (!currentDayList.isEmpty()) {
                currentPlanDay = currentDayList.get(0);

                Hibernate.initialize(currentPlanDay.getWorkouts());
                for (Workout workout : currentPlanDay.getWorkouts()) {
                    Hibernate.initialize(workout.getWorkoutExercises());

                    for (var workoutExercise : workout.getWorkoutExercises()) {
                        Hibernate.initialize(workoutExercise.getExercise());
                    }
                }
            }
        }

        return new TrackerDataDTO(currentPlan, planDaysCount, currentDay, currentPlanDay);
    }

    @Transactional(readOnly = true)
    public Plan getEditable(UUID planId, UUID userId) {
        Plan plan = planRepository.findByIdWithSharedUsers(planId).orElseThrow(() -> new ActivityNotFoundException("Plan not found"));

        if (isOwner(plan, userId)) {
            throw new UnauthorizedResourceAccessException("You don't have permission to edit this activity");
        }

        return getAccessibleByIdWithPlanDays(planId, userService.getById(userId));
    }

    public ModelAndView getTrackerPageModelAndView(Plan activePlan, TrackerDataDTO trackerData, UUID userId) {
        ModelAndView mv = new ModelAndView("fitness/tracker");

        boolean hasActivePlan = activePlan != null;

        mv.addObject("activePlan", activePlan);
        mv.addObject("hasActivePlan", hasActivePlan);
        mv.addObject("activePlanStatus", hasActivePlan ? "YES" : "NO");

        if (hasActivePlan) {
            mv.addObject("activePlanId", activePlan.getId());
            mv.addObject("activePlanName", activePlan.getName());
        }
        mv.addObject("planDaysCount", trackerData.getPlanDaysCount());
        mv.addObject("currentDay", trackerData.getCurrentDay());
        mv.addObject("currentPlanDay", trackerData.getCurrentPlanDay());
        mv.addObject("totalCompletions", completionService.getCompletionCountForUser(userId));
        mv.addObject("recentCompletions", completionService.getRecentCompletionsForUser(userId, 5));

        PlanDay currentPlanDay = trackerData.getCurrentPlanDay();

        List<Workout> currentDayWorkouts;
        if (currentPlanDay != null && currentPlanDay.getWorkouts() != null) {
            currentDayWorkouts = currentPlanDay.getWorkouts();
            mv.addObject("currentDayWorkouts", currentDayWorkouts);
        } else {
            currentDayWorkouts = new ArrayList<Workout>();
            mv.addObject("currentDayWorkouts", currentDayWorkouts);
        }
        if (currentPlanDay != null && currentPlanDay.getWorkouts() != null) {
            Map<String, Boolean> exerciseCompletionStatus = new HashMap<>();
            Map<String, Boolean> workoutCompletionStatus = new HashMap<>();

            int workoutIndex = 0;
            for (Workout workout : currentPlanDay.getWorkouts()) {
                if (workout.getWorkoutExercises() != null) {
                    for (var exercise : workout.getWorkoutExercises()) {
                        boolean isCompleted = completionService.isExerciseCompleted(userId, exercise.getId(), currentPlanDay.getId(), workoutIndex);
                        String compoundKey = exercise.getId() + "_" + workoutIndex;
                        exerciseCompletionStatus.put(compoundKey, Boolean.valueOf(isCompleted));
                    }
                }

                boolean workoutCompleted = completionService.isWorkoutCompleted(userId, workout.getId(), currentPlanDay.getId(), workoutIndex);
                String workoutCompoundKey = workout.getId() + "_" + workoutIndex;
                workoutCompletionStatus.put(workoutCompoundKey, workoutCompleted);
                workoutIndex++;
            }

            mv.addObject("exerciseCompletionStatus", exerciseCompletionStatus);
            mv.addObject("workoutCompletionStatus", workoutCompletionStatus);
        } else {
            mv.addObject("exerciseCompletionStatus", new HashMap<String, Boolean>());
            mv.addObject("workoutCompletionStatus", new HashMap<String, Boolean>());
        }

        return mv;
    }

    @Transactional(readOnly = true)
    public ModelAndView getPlansPageModelAndView(Plan activePlan, UUID userId, TrackerDataDTO trackerData) throws JsonProcessingException {
        ModelAndView mv = new ModelAndView("fitness/plans");

        if (activePlan != null) {
            Plan plan = getAccessibleByIdWithPlanDays(activePlan.getId(), userService.getById(userId));

            Map<UUID, Boolean> planDayCompletionStatus = new HashMap<>();
            for (PlanDay planDay : plan.getPlanDays()) {
                boolean isCompleted = completionService.isPlanDayCompleted(userId, planDay);
                planDayCompletionStatus.put(planDay.getId(), isCompleted);
            }

            mv.addObject("activePlan", plan);
            mv.addObject("planDays", plan.getPlanDays());
            mv.addObject("planDayCompletionStatus", planDayCompletionStatus);
            mv.addObject("currentDay", trackerData.getCurrentDay());
            mv.addObject("totalDays", trackerData.getPlanDaysCount());

            int totalWorkouts = plan.getPlanDays().stream()
                    .mapToInt(day -> day.getWorkouts() != null ? day.getWorkouts().size() : 0)
                    .sum();
            mv.addObject("totalWorkouts", totalWorkouts);

            LocalDate planStartDate = userService.getCurrentPlanStartDate(userId);

            Set<Integer> skippedDayNumbers = new HashSet<>();
            List<PlanDay> planDays = plan.getPlanDays();

            for (PlanDay planDay : planDays) {
                int originalDayNumber = planDay.getDayNumber();

                LocalDate planDayDate = planStartDate.plusDays(originalDayNumber - 1);
                LocalDate today = LocalDate.now();

                if (planDayDate.isBefore(today) && !planDayCompletionStatus.get(planDay.getId())) {
                    skippedDayNumbers.add(originalDayNumber);
                }
            }

            List<Map<String, Object>> simplifiedPlanDays = new ArrayList<>();
            int effectiveDayCounter = 1;

            for (PlanDay planDay : planDays) {
                int originalDayNumber = planDay.getDayNumber();
                boolean isSkipped = skippedDayNumbers.contains(originalDayNumber);

                int completedWorkoutsCount = 0;
                int completedExercisesCount = 0;
                int totalExercisesCount = 0;

                if (planDay.getWorkouts() != null) {
                    for (int i = 0; i < planDay.getWorkouts().size(); i++) {
                        Workout workout = planDay.getWorkouts().get(i);
                        boolean workoutCompleted = completionService.isWorkoutCompleted(userId, workout.getId(), planDay.getId(), i);

                        if (workoutCompleted) {
                            completedWorkoutsCount++;
                        }

                        if (workout.getWorkoutExercises() != null) {
                            totalExercisesCount += workout.getWorkoutExercises().size();

                            for (int j = 0; j < workout.getWorkoutExercises().size(); j++) {
                                var exercise = workout.getWorkoutExercises().get(j);
                                boolean exerciseCompleted = completionService.isExerciseCompleted(userId, exercise.getId(), planDay.getId(), i);
                                if (exerciseCompleted) {
                                    completedExercisesCount++;
                                }
                            }
                        }
                    }
                }

                Map<String, Object> dayData = new HashMap<>();
                dayData.put("id", planDay.getId().toString());
                dayData.put("dayNumber", originalDayNumber);
                dayData.put("originalDayNumber", originalDayNumber);
                dayData.put("effectiveDayNumber", effectiveDayCounter);
                dayData.put("type", planDay.getType().name());
                dayData.put("workoutCount", planDay.getWorkouts() != null ? planDay.getWorkouts().size() : 0);
                dayData.put("completedWorkoutsCount", completedWorkoutsCount);
                dayData.put("totalExercisesCount", totalExercisesCount);
                dayData.put("completedExercisesCount", completedExercisesCount);
                dayData.put("isSkipped", isSkipped);
                dayData.put("isCompleted", planDayCompletionStatus.get(planDay.getId()));

                simplifiedPlanDays.add(dayData);

                if (!isSkipped) {
                    effectiveDayCounter++;
                }
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String planDaysJson = objectMapper.writeValueAsString(simplifiedPlanDays);
            String completionStatusJson = objectMapper.writeValueAsString(planDayCompletionStatus);

            mv.addObject("planDaysJson", planDaysJson);
            mv.addObject("completionStatusJson", completionStatusJson);
            mv.addObject("planStartDate", planStartDate);

        } else {
            List<PlanSummaryDTO> planDtos = getPlanSummariesForUser(userId);
            mv.addObject("planSummaries", planDtos);
            mv.addObject("planCount", planDtos.size());
            mv.addObject("totalWorkouts", planDtos.stream()
                    .mapToInt(PlanSummaryDTO::getTotalWorkouts)
                    .sum());
            mv.addObject("planTypeSummary", planDtos.stream()
                    .map(p -> p.getType().name())
                    .distinct()
                    .collect(Collectors.joining(", ")));

            mv.addObject("planDaysJson", "[]");
            mv.addObject("completionStatusJson", "{}");
            mv.addObject("planStartDate", null);
        }

        return mv;
    }

    public List<Plan> getPendingApproval() {
        return planRepository.findByIsPublicTrueAndModerationStatus(ModerationStatus.PENDING);
    }

    @Transactional
    public void approve(UUID id) {
        Plan plan = planRepository.findById(id).orElseThrow(() -> new ActivityNotFoundException("Plan does not exist"));
        plan.setModerationStatus(ModerationStatus.APPROVED);
        planRepository.save(plan);
    }

    @Transactional
    public void reject(UUID id) {
        Plan plan = planRepository.findById(id).orElseThrow(() -> new ActivityNotFoundException("Plan does not exist"));
        plan.setModerationStatus(ModerationStatus.REJECTED);
        plan.setIsPublic(false);
        planRepository.save(plan);
    }

    public long count() {
        return planRepository.count();
    }
}