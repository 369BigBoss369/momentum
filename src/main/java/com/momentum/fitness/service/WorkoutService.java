package com.momentum.fitness.service;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.exception.UnauthorizedResourceAccessException;
import com.momentum.exception.fitness.ActivityNotFoundException;
import com.momentum.exception.fitness.CustomActivityAlreadyExists;
import com.momentum.fitness.dto.CreateWorkoutDTO;
import com.momentum.fitness.dto.CreateWorkoutExerciseDTO;
import com.momentum.fitness.model.Exercise;
import com.momentum.fitness.model.Workout;
import com.momentum.fitness.model.WorkoutExercise;
import com.momentum.fitness.model.enums.MuscleType;
import com.momentum.fitness.model.enums.SourceType;
import com.momentum.fitness.model.enums.WorkoutType;
import com.momentum.fitness.repository.WorkoutRepository;
import com.momentum.user.model.User;
import com.momentum.user.service.FitnessActivityService;
import com.momentum.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.momentum.util.AccessControlUtil;
import com.momentum.util.FitnessMath;
import com.momentum.util.ModerationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class WorkoutService {
    private final WorkoutRepository workoutRepository;;
    private final ExerciseService exerciseService;
    private final UserService userService;
    private final FitnessActivityService fitnessActivityService;

    private static final Logger logger = LoggerFactory.getLogger(WorkoutService.class);

    @Autowired
    public WorkoutService(WorkoutRepository workoutRepository, ExerciseService exerciseService, UserService userService, FitnessActivityService fitnessActivityService) {
        this.workoutRepository = workoutRepository;
        this.exerciseService = exerciseService;
        this.userService = userService;
        this.fitnessActivityService = fitnessActivityService;
    }

    @Transactional
    public Workout createWorkout(CreateWorkoutDTO createWorkoutDto, UUID userId) {
        logger.info("Service: createWorkout called with auth={}, workoutDto={}", userId, createWorkoutDto);
        logger.info("Service: workout name='{}', type={}, isPublic={}, exercises count={}",
                createWorkoutDto.getName(), createWorkoutDto.getType(), createWorkoutDto.getIsPublic(),
                createWorkoutDto.getExercises() != null ? createWorkoutDto.getExercises().size() : 0);

        Optional<Workout> optionalWorkout = workoutRepository.findByOwnerIdAndName(userId, createWorkoutDto.getName());
        if (optionalWorkout.isPresent()) {
            logger.warn("Service: Workout with name '{}' already exists for user {}", createWorkoutDto.getName(), userId);
            throw new CustomActivityAlreadyExists(String.format("You have already added custom workout with the name '%s'", createWorkoutDto.getName()));
        }

        logger.info("Service: Creating new workout");
        Workout workout = Workout.builder()
                .name(createWorkoutDto.getName())
                .type(createWorkoutDto.getType())
                .build();

        workout.setIsPublic(createWorkoutDto.getIsPublic());
        ModerationUtil.applyPublicityChange(workout, false);
        workout.setOwnerId(userId);
        workout.setSource(SourceType.CUSTOM);

        List<WorkoutExercise> workoutExercises = new ArrayList<>();
        int order = 1;
        logger.info("Service: Processing {} exercises", createWorkoutDto.getExercises().size());
        for (CreateWorkoutExerciseDTO workoutExerciseDto : createWorkoutDto.getExercises()) {
            logger.debug("Service: Processing exercise: id={}, exerciseId={}, reps={}, weight={}, duration={}",
                    workoutExerciseDto.getId(), workoutExerciseDto.getExerciseId(), workoutExerciseDto.getReps(),
                    workoutExerciseDto.getWeight(), workoutExerciseDto.getDuration());

            try {
                Exercise exercise = exerciseService.getById(workoutExerciseDto.getExerciseId());
                logger.debug("Service: Found exercise: {}", exercise.getName());

                Double burnedCalories = FitnessMath.calculateBurnedCalories(exercise, workoutExerciseDto);
                logger.debug("Service: Calculated burned calories: {}", burnedCalories);

                WorkoutExercise workoutExercise = WorkoutExercise.builder()
                        .workout(workout)
                        .exercise(exercise)
                        .number(order++)
                        .reps(workoutExerciseDto.getReps())
                        .weight(workoutExerciseDto.getWeight())
                        .duration(workoutExerciseDto.getDuration())
                        .burnedCalories(burnedCalories)
                        .build();

                workoutExercises.add(workoutExercise);
            } catch (Exception e) {
                logger.warn("Service: Exercise {} not found or error processing: {}", workoutExerciseDto.getExerciseId(), e.getMessage());

                logger.info("Service: Skipping invalid exercise {} and continuing with workout creation", workoutExerciseDto.getExerciseId());
            }
        }
        workout.setWorkoutExercises(workoutExercises);

        logger.info("Service: Saving workout with {} exercises", workoutExercises.size());
        try {
            Workout saved = workoutRepository.save(workout);
            logger.info("Service: Workout saved successfully with id: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            logger.error("Service: Error saving workout: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Workout getById(UUID id) {
        return workoutRepository.findById(id).orElseThrow(() -> new ActivityNotFoundException("Workout does not exist"));
    }

    @Transactional(readOnly = true)
    public Workout getAccessibleById(UUID workoutId, User currentUser) {
        Workout workout = workoutRepository.findByIdWithSharedUsers(workoutId).orElseThrow(() -> new ActivityNotFoundException("Workout not found"));

        if (!AccessControlUtil.canView(workout, currentUser)) {
            throw new ActivityNotFoundException("Workout does not exist");
        }

        return loadFullWorkoutDetails(workout);
    }

    @Transactional(readOnly = true)
    public Workout getFullWorkoutForNestedView(UUID workoutId) {
        Workout workout = workoutRepository.findByIdWithSharedUsers(workoutId).orElseThrow(() -> new ActivityNotFoundException("Workout not found"));
        return loadFullWorkoutDetails(workout);
    }

    private Workout loadFullWorkoutDetails(Workout workout) {
        List<WorkoutExercise> exercises = workoutRepository.findExercisesByWorkoutId(workout.getId());

        List<UUID> exerciseIds = exercises.stream()
                .map(we -> we.getExercise().getId())
                .distinct()
                .collect(java.util.stream.Collectors.toList());

        if (!exerciseIds.isEmpty()) {
            List<Exercise> exercisesWithMuscleGroups = exerciseService.getAllByIdWithMuscleGroups(exerciseIds);

            Map<UUID, Exercise> exerciseMap = exercisesWithMuscleGroups.stream()
                    .collect(Collectors.toMap(Exercise::getId, exercise -> exercise));

            for (WorkoutExercise we : exercises) {
                Exercise loadedExercise = exerciseMap.get(we.getExercise().getId());

                if (loadedExercise != null) {
                    we.setExercise(loadedExercise);
                } else {
                    logger.warn("Service: Exercise {} not found when loading workout {}", we.getExercise().getId(), workout.getId());
                }
                we.setWorkout(workout);
            }
        } else {
            exercises.forEach(we -> we.setWorkout(workout));
        }

        workout.setWorkoutExercises(exercises);
        return workout;
    }

    @Transactional(readOnly = true)
    public List<Workout> searchByName(String query, int limit, UUID userId) {
        List<Workout> workouts;

        if (query == null) {
            workouts = workoutRepository.findAllAccessible(userId).stream()
                    .limit(Math.max(1, Math.min(limit, 50)))
                    .collect(Collectors.toList());
        } else {
            workouts = workoutRepository.searchByName(query.trim(), userId).stream()
                    .limit(Math.max(1, Math.min(limit, 50)))
                    .collect(Collectors.toList());
        }

        if (!workouts.isEmpty()) {
            List<UUID> workoutIds = workouts.stream().map(Workout::getId).collect(Collectors.toList());
            List<WorkoutExercise> allExercises = workoutRepository.findExercisesByWorkoutIds(workoutIds);

            Map<UUID, List<WorkoutExercise>> exercisesByWorkoutId = allExercises.stream()
                    .collect(Collectors.groupingBy(we -> we.getWorkout().getId()));

            for (Workout workout : workouts) {
                List<WorkoutExercise> workoutExercises = exercisesByWorkoutId.getOrDefault(workout.getId(), new ArrayList<>());
                workout.setWorkoutExercises(workoutExercises);
            }
        }

        return workouts;
    }

    @Transactional(readOnly = true)
    public Page<Workout> search(String query, WorkoutType workoutType, UUID userId, Pageable pageable) {
        return workoutRepository.search(query, workoutType, userId, pageable);
    }

    public boolean isOwner(Workout workout, UUID userId) {
        return userId.equals(workout.getOwnerId());
    }

    @Transactional
    public Workout updateWorkout(UUID workoutId, CreateWorkoutDTO updateDto, UUID  userId) {
        Workout workout = getEditable(workoutId, userId);

        if (!isOwner(workout, userId)) {
            throw new UnauthorizedResourceAccessException("You do not have permission to modify this workout");
        }

        workout.setName(updateDto.getName());
        workout.setType(updateDto.getType());

        boolean wasPublic = Boolean.TRUE.equals(workout.getIsPublic());
        workout.setIsPublic(updateDto.getIsPublic());
        ModerationUtil.applyPublicityChange(workout, wasPublic);

        if (updateDto.getExercises() != null) {
            List<WorkoutExercise> updatedExercises = new ArrayList<>();

            Map<UUID, WorkoutExercise> existingExercises = workout.getWorkoutExercises().stream()
                    .collect(Collectors.toMap(WorkoutExercise::getId, we -> we));

            int order = 1;
            for (CreateWorkoutExerciseDTO dto : updateDto.getExercises()) {
                
                try {
                    Exercise exercise = exerciseService.getById(dto.getExerciseId());
                    Double burnedCalories = FitnessMath.calculateBurnedCalories(exercise, dto);

                    if (dto.getId() != null && existingExercises.containsKey(dto.getId())) {
                        WorkoutExercise workoutExercise = existingExercises.get(dto.getId());
                        workoutExercise.setExercise(exercise);
                        workoutExercise.setNumber(order++);
                        workoutExercise.setReps(dto.getReps());
                        workoutExercise.setWeight(dto.getWeight());
                        workoutExercise.setDuration(dto.getDuration());
                        workoutExercise.setBurnedCalories(burnedCalories);
                        updatedExercises.add(workoutExercise);
                    } else {
                        WorkoutExercise workoutExercise = WorkoutExercise.builder()
                                .workout(workout)
                                .exercise(exercise)
                                .number(order++)
                                .reps(dto.getReps())
                                .weight(dto.getWeight())
                                .duration(dto.getDuration())
                                .burnedCalories(burnedCalories)
                                .build();
                        updatedExercises.add(workoutExercise);
                    }
                } catch (Exception e) {
                    logger.warn("Service: Exercise {} not found or error processing: {}", dto.getExerciseId(), e.getMessage());
                    logger.info("Service: Skipping invalid exercise {} and continuing with workout update", dto.getExerciseId());
                }
            }

            workout.getWorkoutExercises().clear();
            workout.getWorkoutExercises().addAll(updatedExercises);
        }

        return workoutRepository.save(workout);
    }

    public boolean isInLibrary(UUID workoutId, UUID userId) {
        return workoutRepository.existsByIdAndSharedUsers_Id(workoutId, userId);
    }

    @Transactional
    public void addToLibrary(UUID workoutId, UUID userId) {
        Workout workout = workoutRepository.findByIdWithSharedUsers(workoutId).orElseThrow(() -> new ActivityNotFoundException("Workout not found"));

        if (!Boolean.TRUE.equals(workout.getIsPublic()) || !ModerationUtil.isVisible(workout)) {
            throw new IllegalStateException("This item is not yet approved and cannot be added to a library");
        }

        User user = userService.getById(userId);

        boolean alreadyShared = workout.getSharedUsers().stream().anyMatch(u -> u.getId().equals(userId));
        if (!alreadyShared) {
            workout.getSharedUsers().add(user);
            workoutRepository.save(workout);
            fitnessActivityService.logWorkoutAddedToLibrary(userId, workout.getName());
        }
    }

    @Transactional
    public void removeFromLibrary(UUID workoutId, UUID userId) {
        Workout workout = workoutRepository.findByIdWithSharedUsers(workoutId).orElseThrow(() -> new ActivityNotFoundException("Workout not found"));

        boolean removed = workout.getSharedUsers().removeIf(u -> u.getId().equals(userId));
        if (removed) {
            workoutRepository.save(workout);
            fitnessActivityService.logWorkoutRemovedFromLibrary(userId, workout.getName());
        }
    }

    public List<WorkoutExercise> getExercisesByWorkoutId(UUID workoutId) {
        return workoutRepository.findExercisesByWorkoutId(workoutId);
    }

    @Transactional(readOnly = true)
    public Workout getEditable(UUID workoutId, UUID userId) {
        Workout workout = workoutRepository.findByIdWithSharedUsers(workoutId).orElseThrow(() -> new ActivityNotFoundException("Workout not found"));

        if (!isOwner(workout, userId)) {
            throw new UnauthorizedResourceAccessException("You don't have permission to edit this activity");
        }

        return getAccessibleById(workout.getId(), userService.getById(userId));
    }

    public List<String> getAvailableWorkoutNamesByMuscleGroups(UUID userId, List<MuscleType> muscles) {
        if (muscles == null || muscles.isEmpty()) {
            return List.of();
        }
        return workoutRepository.findAccessibleByMuscleGroups(userId, muscles)
                .stream()
                .map(Workout::getName)
                .distinct()
                .toList();
    }

    public Optional<Workout> findAccessibleByName(UUID userId, String name) {
        return workoutRepository.findAccessibleByName(userId, name);
    }

    public List<Workout> getPendingApproval() {
        return workoutRepository.findByIsPublicTrueAndModerationStatus(ModerationStatus.PENDING);
    }

    @Transactional
    public void approve(UUID id) {
        Workout workout = workoutRepository.findById(id).orElseThrow(() -> new ActivityNotFoundException("Workout does not exist"));
        workout.setModerationStatus(ModerationStatus.APPROVED);
        workoutRepository.save(workout);
    }

    @Transactional
    public void reject(UUID id) {
        Workout workout = workoutRepository.findById(id).orElseThrow(() -> new ActivityNotFoundException("Workout does not exist"));
        workout.setModerationStatus(ModerationStatus.REJECTED);
        workout.setIsPublic(false);
        workoutRepository.save(workout);
    }

    public long count() {
        return workoutRepository.count();
    }
}

