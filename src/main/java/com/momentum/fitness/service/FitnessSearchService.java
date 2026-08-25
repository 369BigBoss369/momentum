package com.momentum.fitness.service;

import com.momentum.core.model.ShareableEntity;
import com.momentum.fitness.dto.ActivitySearchDTO;
import com.momentum.fitness.dto.enums.OwnerShipType;
import com.momentum.fitness.repository.PlanDayRepository;
import com.momentum.fitness.repository.WorkoutRepository;
import com.momentum.fitness.model.Exercise;
import com.momentum.fitness.model.Plan;
import com.momentum.fitness.model.Workout;
import com.momentum.fitness.model.WorkoutExercise;
import com.momentum.fitness.model.enums.ExerciseType;
import com.momentum.fitness.model.enums.PlanType;
import com.momentum.fitness.model.enums.WorkoutType;
import com.momentum.user.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FitnessSearchService {
    private static final Logger logger = LoggerFactory.getLogger(FitnessSearchService.class);

    private final ExerciseService exerciseService;
    private final WorkoutService workoutService;
    private final PlanService planService;
    private final WorkoutRepository workoutRepository;
    private final PlanDayRepository planDayRepository;

    @Autowired
    public FitnessSearchService(ExerciseService exerciseService, WorkoutService workoutService, PlanService planService, WorkoutRepository workoutRepository, PlanDayRepository planDayRepository) {
        this.exerciseService = exerciseService;
        this.workoutService = workoutService;
        this.planService = planService;
        this.workoutRepository = workoutRepository;
        this.planDayRepository = planDayRepository;
    }

    @Transactional(readOnly = true)
    public List<ActivitySearchDTO> search(String query, String owner, String type, String activityType, Boolean inLibrary, int limit, UUID userId) {
        logger.info("Starting search: query='{}', owner='{}', type='{}', activityType='{}', inLibrary={}, limit={}, userId={}",
                   query, owner, type, activityType, inLibrary, limit, userId);

        List<ActivitySearchDTO> results = new ArrayList<>();
        OwnerShipType ownership = OwnerShipType.valueOf(owner.toUpperCase());

        try {
            if (type.equals("EXERCISE") || type.equals("ALL")) {
                logger.info("Searching for exercises...");
                List<ActivitySearchDTO> exerciseResults = searchExercises(query, ownership, activityType, inLibrary, limit, userId);
                logger.info("Found {} exercises", exerciseResults.size());
                results.addAll(exerciseResults);
            }

            if (type.equals("WORKOUT") || type.equals("ALL")) {
                logger.info("Searching for workouts...");
                List<ActivitySearchDTO> workoutResults = searchWorkouts(query, ownership, activityType, inLibrary, limit, userId);
                logger.info("Found {} workouts", workoutResults.size());
                results.addAll(workoutResults);
            }

            if (type.equals("PLAN") || type.equals("ALL")) {
                logger.info("Searching for plans...");
                List<ActivitySearchDTO> planResults = searchPlans(query, ownership, activityType, inLibrary, limit, userId);
                logger.info("Found {} plans", planResults.size());
                results.addAll(planResults);
            }

        } catch (Exception e) {
            logger.error("Error in combined search", e);
        }

        logger.info("Total search results: {}", results.size());
        return results;
    }

    private List<ActivitySearchDTO> searchExercises(String query, OwnerShipType ownership, String activityType, Boolean inLibrary, int limit, UUID userId) {
        logger.info("searchExercises: query='{}', ownership={}, activityType='{}', inLibrary={}, limit={}, userId={}",
                   query, ownership, activityType, inLibrary, limit, userId);

        List<ActivitySearchDTO> exerciseResults = new ArrayList<>();

        ExerciseType type = activityType.equals("ALL") ? null : ExerciseType.valueOf(activityType);
        Pageable pageable = PageRequest.of(0, limit);

        logger.info("Calling exerciseService.search with query={}, type={}, userId={}, pageable={}",
                   query != null && !query.trim().isEmpty() ? query.trim() : null, type, userId, pageable);

        List<Exercise> exercises = exerciseService.search(
                query != null && !query.trim().isEmpty() ? query.trim() : null,
                type,
                userId,
                pageable
        ).getContent();

        logger.info("exerciseService.search returned {} exercises", exercises.size());

        exercises = filterByOwnership(exercises, userId, ownership);
        exercises = filterByLibrary(exercises, userId, ownership, inLibrary);

        logger.info("After filtering, {} exercises remain for user {}", exercises.size(), userId);

        List<UUID> exerciseIds = exercises.stream().map(Exercise::getId).collect(Collectors.toList());
        if (!exerciseIds.isEmpty()) {

            List<Exercise> exercisesWithMuscleGroups = exerciseService.getAllByIdWithMuscleGroups(exerciseIds);
            List<Exercise> exercisesWithSharedUsers = exerciseService.getAllByIdWithSharedUsers(exerciseIds);


            Map<UUID, List<String>> muscleGroupsMap = exercisesWithMuscleGroups.stream()
                    .collect(Collectors.toMap(
                            Exercise::getId,
                            exercise -> exercise.getMuscleGroupTarget().stream()
                                    .map(mt -> mt.getMuscle().getDisplayName())
                                    .distinct()
                                    .collect(Collectors.toList())
                    ));

            Map<UUID, Set<User>> sharedUsersMap = exercisesWithSharedUsers.stream()
                    .collect(Collectors.toMap(
                            Exercise::getId,
                            Exercise::getSharedUsers
                    ));

            for (Exercise exercise : exercises) {
                List<String> muscleGroups = muscleGroupsMap.get(exercise.getId());
                if (muscleGroups == null) {
                    muscleGroups = java.util.Collections.emptyList();
                }


                Set<User> sharedUsers = sharedUsersMap.get(exercise.getId());
                if (sharedUsers == null) {
                    sharedUsers = exercise.getSharedUsers();
                }

                exerciseResults.add(buildExerciseSearchDTO(exercise, userId, muscleGroups, sharedUsers));
            }
        } else {
            for (Exercise exercise : exercises) {
                exerciseResults.add(buildExerciseSearchDTO(exercise, userId, java.util.Collections.emptyList(), exercise.getSharedUsers()));
            }

            for (Exercise exercise : exercises) {
                exerciseResults.add(buildExerciseSearchDTO(exercise, userId, java.util.Collections.emptyList(), exercise.getSharedUsers()));
            }
        }

        return exerciseResults;
    }

    private List<ActivitySearchDTO> searchWorkouts(String query, OwnerShipType ownership, String activityType, Boolean inLibrary, int limit, UUID userId) {
        List<ActivitySearchDTO> workoutResults = new ArrayList<>();

        WorkoutType type = activityType.equals("ALL") ? null : WorkoutType.valueOf(activityType);
        Pageable pageable = PageRequest.of(0, limit);

        List<Workout> workouts = workoutService.search(
                query != null && !query.trim().isEmpty() ? query.trim() : null,
                type,
                userId,
                pageable
        ).getContent();

        workouts = filterByOwnership(workouts, userId, ownership);
        workouts = filterByLibrary(workouts, userId, ownership, inLibrary);

        logger.info("After filtering, {} workouts remain for user {}", workouts.size(), userId);


        if (!workouts.isEmpty()) {
            List<UUID> workoutIds = workouts.stream().map(Workout::getId).collect(Collectors.toList());
            List<WorkoutExercise> allExercises = workoutRepository.findExercisesByWorkoutIds(workoutIds);


            Map<UUID, List<WorkoutExercise>> exercisesByWorkoutId = allExercises.stream()
                    .collect(Collectors.groupingBy(we -> we.getWorkout().getId()));


            for (Workout workout : workouts) {
                List<WorkoutExercise> workoutExercises = exercisesByWorkoutId.getOrDefault(workout.getId(), new ArrayList<>());
                workout.setWorkoutExercises(workoutExercises);
                int exerciseCount = workoutExercises.size();
                logger.debug("Workout {} has {} exercises", workout.getName(), exerciseCount);
                workoutResults.add(buildWorkoutSearchDTO(workout, userId, exerciseCount));
            }
        } else {
            for (Workout workout : workouts) {
                workoutResults.add(buildWorkoutSearchDTO(workout, userId, 0));
            }
        }

        return workoutResults;
    }

    private List<ActivitySearchDTO> searchPlans(String query, OwnerShipType ownership, String activityType, Boolean inLibrary, int limit, UUID userId) {
        List<ActivitySearchDTO> planResults = new ArrayList<>();

        PlanType type = activityType.equals("ALL") ? null : PlanType.valueOf(activityType);
        Pageable pageable = PageRequest.of(0, limit);

        List<Plan> plans = planService.search(
                query != null && !query.trim().isEmpty() ? query.trim() : null,
                type,
                userId,
                pageable
        ).getContent();

        plans = filterByOwnership(plans, userId, ownership);
        plans = filterByLibrary(plans, userId, ownership, inLibrary);

        logger.info("After filtering, {} plans remain for user {}", plans.size(), userId);


        for (Plan plan : plans) {
            List<com.momentum.fitness.model.PlanDay> planDays = plan.getPlanDays();
            if (planDays == null || planDays.isEmpty()) {

                planDays = planDayRepository.getByPlan_Id(plan.getId());
                logger.info("Plan {} fallback loaded {} plan days from repository", plan.getName(), planDays.size());
                if (planDays.isEmpty()) {

                    long totalPlanDays = planDayRepository.countAll();
                    List<com.momentum.fitness.model.PlanDay> allPlanDays = planDayRepository.findAll();
                    long orphanedDays = allPlanDays.stream()
                        .filter(pd -> pd.getPlan() == null)
                        .count();
                    logger.warn("Plan {} has no linked plan days. Total plan days in DB: {}, orphaned: {}", plan.getName(), totalPlanDays, orphanedDays);
                }
            } else {
                logger.info("Plan {} has {} plan days loaded from search query", plan.getName(), planDays.size());
            }

            int totalWorkouts = planDays.stream()
                    .mapToInt(day -> day.getWorkouts() != null ? day.getWorkouts().size() : 0)
                    .sum();

            logger.info("Plan {} has {} total workouts across {} days", plan.getName(), totalWorkouts, planDays.size());
            planResults.add(buildPlanSearchDTO(plan, userId, planDays.size(), planDays.size()));
        }

        return planResults;
    }

    private <T extends ShareableEntity> List<T> filterByOwnership(List<T> activities, UUID userId, OwnerShipType ownership) {
        return activities.stream()
                .filter(activity -> {
                    boolean isOwner = userId.equals(activity.getOwnerId());
                    boolean isShared = activity.getSharedUsers() != null &&
                                     activity.getSharedUsers().stream().anyMatch(u -> userId.equals(u.getId()));

                    if (ownership == OwnerShipType.ALL) {
                        return true;
                    } else if (ownership == OwnerShipType.OWN) {
                        return isOwner;
                    } else if (ownership == OwnerShipType.OTHERS) {
                        if (isOwner) {
                            return false;
                        }

                        String source = activity.getSource().name();
                        if ("DEFAULT".equals(source)) {
                            return true;
                        }

                        if ("CUSTOM".equals(source)) {
                            return activity.getIsPublic() || isShared;
                        }

                        if ("SHARED".equals(source)) {
                            return isShared;
                        }

                        return false;
                    }
                    return true;
                })
                .toList();
    }

    private <T extends ShareableEntity> List<T> filterByLibrary(List<T> activities, UUID userId, OwnerShipType ownership, Boolean inLibrary) {
        if (ownership != OwnerShipType.OTHERS || inLibrary == null) {
            return activities;
        }

        return activities.stream()
                .filter(activity -> {
                    boolean isOwner = activity.getOwnerId().equals(userId);
                    boolean isShared = activity.getSharedUsers() != null &&
                                     activity.getSharedUsers().stream().anyMatch(u -> u.getId().equals(userId));

                    if (inLibrary) {
                        return isOwner || isShared;
                    } else {
                        return !isOwner && !isShared;
                    }
                })
                .toList();
    }

    private ActivitySearchDTO buildExerciseSearchDTO(Exercise exercise, UUID userId, List<String> muscleGroups, Set<User> sharedUsers) {
        boolean isOwner = exercise.getOwnerId().equals(userId);
        boolean isInLibrary = !isOwner && sharedUsers != null && sharedUsers.stream().anyMatch(u -> u.getId().equals(userId));
        String ownerUsername = isOwner ? "owned" : null;

        int sharedUsersCount = sharedUsers != null ? sharedUsers.size() : 0;
        logger.debug("Exercise {} has {} shared users", exercise.getName(), sharedUsersCount);

        return ActivitySearchDTO.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .type("EXERCISE")
                .typeDisplayName(exercise.getType().getDisplayName())
                .imageUrl(exercise.getImageUrl())
                .muscleGroups(muscleGroups)
                .owner(ownerUsername)
                .isPublic(exercise.getIsPublic())
                .inLibrary(isInLibrary)
                .sharedUsers(sharedUsersCount)
                .build();
    }

    private ActivitySearchDTO buildWorkoutSearchDTO(Workout workout, UUID userId, int exerciseCount) {
        Set<User> sharedUsers = workout.getSharedUsers();

        boolean isOwner = workout.getOwnerId().equals(userId);
        boolean isInLibrary = !isOwner && sharedUsers.stream().anyMatch(u -> u.getId().equals(userId));
        String ownerUsername = isOwner ? "owned" : null;

        return ActivitySearchDTO.builder()
                .id(workout.getId())
                .name(workout.getName())
                .type("WORKOUT")
                .typeDisplayName(workout.getType().getDisplayName())
                .exerciseCount(exerciseCount)
                .isPublic(workout.getIsPublic())
                .exercises(Collections.emptyList())
                .owner(ownerUsername)
                .inLibrary(isInLibrary)
                .sharedUsers(sharedUsers.size())
                .build();
    }

    private ActivitySearchDTO buildPlanSearchDTO(Plan plan, UUID userId, int dayCount, int totalWorkouts) {
        Set<User> sharedUsers = plan.getSharedUsers();

        boolean isOwner = plan.getOwnerId().equals(userId);
        boolean isInLibrary = !isOwner && sharedUsers.stream().anyMatch(u -> u.getId().equals(userId));
        String ownerUsername = isOwner ? "owned" : null;

        return ActivitySearchDTO.builder()
                .id(plan.getId())
                .name(plan.getName())
                .type("PLAN")
                .typeDisplayName(plan.getType().getDisplayName())
                .exerciseCount(dayCount)
                .isPublic(plan.getIsPublic())
                .exercises(Collections.emptyList())
                .owner(ownerUsername)
                .inLibrary(isInLibrary)
                .sharedUsers(sharedUsers.size())
                .build();
    }
}

