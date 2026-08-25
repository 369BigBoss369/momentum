package com.momentum.fitness.service;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.exception.fitness.ActivityNotFoundException;
import com.momentum.exception.fitness.CustomActivityAlreadyExists;
import com.momentum.exception.UnauthorizedResourceAccessException;
import com.momentum.exception.nutrition.FoodNotFoundException;
import com.momentum.fitness.dto.CreateExerciseDTO;
import com.momentum.fitness.model.Exercise;
import com.momentum.fitness.model.enums.ExerciseType;
import com.momentum.fitness.model.enums.MuscleType;
import com.momentum.fitness.model.enums.SourceType;
import com.momentum.fitness.repository.ExerciseRepository;
import com.momentum.user.model.User;
import com.momentum.user.service.FitnessActivityService;
import com.momentum.user.service.UserService;
import com.momentum.util.AccessControlUtil;
import com.momentum.util.ModerationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final UserService userService;
    private final FitnessActivityService fitnessActivityService;

    @Autowired
    public ExerciseService(ExerciseRepository exerciseRepository, UserService userService, FitnessActivityService fitnessActivityService) {
        this.exerciseRepository = exerciseRepository;
        this.userService = userService;
        this.fitnessActivityService = fitnessActivityService;
    }

    @Transactional
    public Exercise createExercise(CreateExerciseDTO createExerciseDto, UUID userId) {
        log.info("Creating exercise: {} for user: {}", createExerciseDto.getName(), userId);

        Optional<Exercise> optional = exerciseRepository.findByOwnerIdAndName(userId, createExerciseDto.getName());
        if (optional.isPresent()) {
            log.warn("Exercise creation failed - exercise with name '{}' already exists for user: {}", createExerciseDto.getName(), userId);
            throw new CustomActivityAlreadyExists(String.format("You have already created custom exercise with the name '%s'", createExerciseDto.getName()));
        }

        Exercise exercise = Exercise.builder()
                .name(createExerciseDto.getName())
                .type(createExerciseDto.getType())
                .imageUrl(createExerciseDto.getImageUrl())
                .videoUrl(createExerciseDto.getVideoUrl())
                .build();

        exercise.setIsPublic(createExerciseDto.getIsPublic());
        ModerationUtil.applyPublicityChange(exercise, false);
        exercise.setOwnerId(userId);
        exercise.setSource(SourceType.CUSTOM);

        exercise.setMuscleGroupTarget(createExerciseDto.getMuscleTargets());

        Exercise saved = exerciseRepository.save(exercise);
        log.info("Exercise created successfully: {} (ID: {}) for user: {}", createExerciseDto.getName(), exercise.getId(), userId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Exercise> searchByName(String query, int limit, UUID userId) {
        Pageable pageable = PageRequest.of(0, Math.max(1, Math.min(limit, 50)), Sort.by("name"));

        if (query == null || query.trim().length() < 2) {
            return exerciseRepository.findAllAccessible(userId, userId, pageable).getContent();
        }

        return exerciseRepository.searchByName(query.trim(), userId, userId, pageable).getContent();
    }

    @Transactional(readOnly = true)
    public Page<Exercise> search(String query, ExerciseType exerciseType, UUID userId, Pageable pageable) {
        return exerciseRepository.search(query, exerciseType, userId, pageable);
    }

    public Exercise getById(UUID id) {
        
        Exercise exercise = exerciseRepository.findById(id).orElseThrow(() -> new ActivityNotFoundException("Exercise does not exist"));
        
        return exercise;
    }

    @Transactional(readOnly = true)
    public Exercise getByIdWithMuscleTargets(UUID id, UUID userId) {
        Exercise exercise = exerciseRepository.findByIdWithMuscleTargets(id).orElseThrow(() -> new ActivityNotFoundException("Exercise does not exist"));

        if (!isOwner(exercise, userId)) {
            throw new UnauthorizedResourceAccessException("You don't have permission to edit this activity");
        }

        return exercise;
    }

    @Transactional(readOnly = true)
    public Exercise getAccessibleById(UUID exerciseId, User currentUser) {
        Exercise exercise = exerciseRepository.findByIdWithMuscleTargetsForViewing(exerciseId).orElseThrow(() -> new ActivityNotFoundException("Exercise does not exist"));

        if (!AccessControlUtil.canView(exercise, currentUser)) {
            throw new ActivityNotFoundException("Exercise does not exist");
        }

        return exercise;
    }

    public boolean isOwner(Exercise exercise, UUID userId) {
        return userId.equals(exercise.getOwnerId());
    }

    @Transactional
    public Exercise updateExercise(UUID exerciseId, CreateExerciseDTO updateDto, UUID userId) {
        Exercise exercise = getById(exerciseId);

        if (!isOwner(exercise, userId)) {
            throw new UnauthorizedResourceAccessException("You do not have permission to modify this activity");
        }

        exercise.setName(updateDto.getName());
        exercise.setType(updateDto.getType());
        exercise.setImageUrl(updateDto.getImageUrl());
        exercise.setVideoUrl(updateDto.getVideoUrl());

        boolean wasPublic = Boolean.TRUE.equals(exercise.getIsPublic());
        exercise.setIsPublic(updateDto.getIsPublic());
        ModerationUtil.applyPublicityChange(exercise, wasPublic);

        exercise.setMuscleGroupTarget(updateDto.getMuscleTargets());

        return exerciseRepository.save(exercise);
    }

    public boolean isInLibrary(UUID exerciseId, UUID userId) {
        return exerciseRepository.existsByIdAndSharedUsers_Id(exerciseId, userId);
    }

    @Transactional
    public void addToLibrary(UUID exerciseId, UUID userId) {
        Exercise exercise = exerciseRepository.findByIdWithSharedUsers(exerciseId).orElseThrow(() -> new ActivityNotFoundException("Exercise not found"));

        if (!Boolean.TRUE.equals(exercise.getIsPublic()) || !ModerationUtil.isVisible(exercise)) {
            throw new IllegalStateException("This item is not yet approved and cannot be added to a library");
        }

        User user = userService.getById(userId);

        boolean alreadyShared = exercise.getSharedUsers().stream().anyMatch(u -> u.getId().equals(userId));
        if (!alreadyShared) {
            exercise.getSharedUsers().add(user);
            exerciseRepository.save(exercise);
            fitnessActivityService.logExerciseAddedToLibrary(userId, exercise.getName());
        }
    }

    @Transactional
    public void removeFromLibrary(UUID exerciseId, UUID userId) {
        Exercise exercise = exerciseRepository.findByIdWithSharedUsers(exerciseId).orElseThrow(() -> new ActivityNotFoundException("Exercise not found"));

        boolean removed = exercise.getSharedUsers().removeIf(u -> u.getId().equals(userId));
        if (removed) {
            exerciseRepository.save(exercise);
            fitnessActivityService.logExerciseRemovedFromLibrary(userId, exercise.getName());
        }
    }

    public List<Exercise> getPendingApproval() {
        return exerciseRepository.findByIsPublicTrueAndModerationStatus(ModerationStatus.PENDING);
    }

    @Transactional
    public void approve(UUID id) {
        Exercise exercise = exerciseRepository.findById(id).orElseThrow(() -> new ActivityNotFoundException("Exercise does not exist"));
        exercise.setModerationStatus(ModerationStatus.APPROVED);
        exerciseRepository.save(exercise);
    }

    @Transactional
    public void reject(UUID id) {
        Exercise exercise = exerciseRepository.findById(id).orElseThrow(() -> new ActivityNotFoundException("Exercise does not exist"));
        exercise.setModerationStatus(ModerationStatus.REJECTED);
        exercise.setIsPublic(false);
        exerciseRepository.save(exercise);
    }

    public List<Exercise> getAllByIdWithMuscleGroups(List<UUID> exerciseIds) {
        return exerciseRepository.findAllByIdWithMuscleGroups(exerciseIds);
    }

    public List<Exercise> getAllByIdWithSharedUsers(List<UUID> exerciseIds) {
        return exerciseRepository.findAllByIdWithSharedUsers(exerciseIds);
    }

    public List<String> getAvailableExerciseNamesByMuscleGroups(UUID userId, List<MuscleType> muscles) {
        if (muscles == null || muscles.isEmpty()) {
            return List.of();
        }
        return exerciseRepository.findAccessibleByMuscleGroups(userId, userId, muscles)
                .stream()
                .map(Exercise::getName)
                .distinct()
                .toList();
    }

    public Optional<Exercise> findAccessibleByName(UUID userId, String name) {
        return exerciseRepository.findAccessibleByName(userId, userId, name);
    }

    public long count() {
        return exerciseRepository.count();
    }
}

