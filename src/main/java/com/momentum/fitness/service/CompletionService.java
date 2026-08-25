package com.momentum.fitness.service;

import com.momentum.fitness.model.Completion;
import com.momentum.fitness.model.PlanDay;
import com.momentum.fitness.model.Workout;
import com.momentum.fitness.model.WorkoutExercise;
import com.momentum.fitness.model.enums.CompletionType;
import com.momentum.fitness.repository.CompletionRepository;
import com.momentum.fitness.repository.WorkoutRepository;
import com.momentum.user.model.User;
import com.momentum.util.FitnessMath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CompletionService {
    private final CompletionRepository completionRepository;
    private final WorkoutRepository workoutRepository;

    @Autowired
    public CompletionService(CompletionRepository completionRepository, WorkoutRepository workoutRepository) {
        this.completionRepository = completionRepository;
        this.workoutRepository = workoutRepository;
    }

    @Cacheable(value = "userStats", key = "'completionCount_' + #userId")
    public long getCompletionCountForUser(UUID userId) {
        return completionRepository.countByUserId(userId);
    }

    @Cacheable(value = "completions", key = "'recent_' + #userId + '_' + #limit")
    public List<Completion> getRecentCompletionsForUser(UUID userId, int limit) {
        return completionRepository.findByUserIdOrderByCompletedAtDesc(userId, limit);
    }

    @Caching(evict = {
            @CacheEvict(value = "userStats", allEntries = true),
            @CacheEvict(value = "completions", allEntries = true)
    })
    public Completion markAsCompleted(User user, UUID targetId, String type, UUID planDayId, Integer workoutPosition) {
        CompletionType completionType = CompletionType.valueOf(type.toUpperCase());

        Completion completion = Completion.builder()
                .user(user)
                .type(completionType)
                .targetId(targetId)
                .planDayId(planDayId)
                .workoutPosition(workoutPosition)
                .build();

        Completion saved = completionRepository.save(completion);
        return saved;
    }

    @Caching(evict = {
            @CacheEvict(value = "userStats", allEntries = true),
            @CacheEvict(value = "completions", allEntries = true)
    })
    public Completion markWorkoutAsCompleted(User user, UUID workoutId, UUID planDayId, Integer workoutPosition) {

        Completion completion = Completion.builder()
                .user(user)
                .type(CompletionType.WORKOUT)
                .targetId(workoutId)
                .planDayId(planDayId)
                .workoutPosition(workoutPosition)
                .build();

        Completion saved = completionRepository.save(completion);
        return saved;
    }

    public boolean isWorkoutCompleted(UUID userId, UUID workoutId, UUID planDayId, Integer workoutPosition) {
        return completionRepository.existsByUserIdAndTargetIdAndTypeAndPlanDayIdAndWorkoutPosition(userId, workoutId, CompletionType.WORKOUT, planDayId, workoutPosition);
    }

    public boolean isPlanDayCompleted(UUID userId, PlanDay planDay) {
        if (planDay == null || planDay.getWorkouts() == null || planDay.getWorkouts().isEmpty()) {
            return false;
        }

        for (int i = 0; i < planDay.getWorkouts().size(); i++) {
            Workout workout = planDay.getWorkouts().get(i);
            boolean workoutCompleted = completionRepository.existsByUserIdAndTargetIdAndTypeAndPlanDayIdAndWorkoutPosition(userId, workout.getId(), CompletionType.WORKOUT, planDay.getId(), i);

            if (!workoutCompleted) {
                return false;
            }
        }

        
        return true;
    }

    @Caching(evict = {
            @CacheEvict(value = "userStats", allEntries = true),
            @CacheEvict(value = "completions", allEntries = true)
    })
    public Completion markPlanDayAsCompleted(User user, UUID planDayId) {

        Completion completion = Completion.builder()
                .user(user)
                .type(CompletionType.PLAN_DAY)
                .targetId(planDayId)
                .planDayId(planDayId)
                .workoutPosition(null)
                .build();

        return completionRepository.save(completion);
    }

    public boolean isPlanDayCompleted(UUID userId, UUID planDayId) {
        return completionRepository.existsByUserIdAndTargetIdAndType(userId, planDayId, CompletionType.PLAN_DAY);
    }

    public boolean isExerciseCompleted(UUID userId, UUID exerciseId, UUID planDayId, Integer workoutPosition) {
        boolean result = completionRepository.existsByUserIdAndTargetIdAndTypeAndPlanDayIdAndWorkoutPosition(userId, exerciseId, CompletionType.EXERCISE, planDayId, workoutPosition);
        System.out.println("DEBUG: Checking completion - User: " + userId + ", Exercise: " + exerciseId + ", PlanDay: " + planDayId + ", Position: " + workoutPosition + ", Result: " + result);

        long totalCompletions = completionRepository.countByUserIdAndTargetIdAndTypeAndPlanDayId(userId, exerciseId, CompletionType.EXERCISE, planDayId);
        System.out.println("DEBUG: Total completions for this exercise in plan day (any position): " + totalCompletions);

        return result;
    }

    public void deleteCompletionsForWorkout(UUID userId, UUID workoutId, UUID planDayId, Integer workoutPosition) {

        completionRepository.deleteByUserIdAndTargetIdAndTypeAndPlanDayIdAndWorkoutPosition(userId, workoutId, CompletionType.WORKOUT, planDayId, workoutPosition);

        var workoutExercises = workoutRepository.findExercisesByWorkoutId(workoutId);
        for (var workoutExercise : workoutExercises) {
            completionRepository.deleteByUserIdAndTargetIdAndTypeAndPlanDayIdAndWorkoutPosition(userId, workoutExercise.getId(), CompletionType.EXERCISE, planDayId, workoutPosition);
        }
    }

    @Transactional
    public void deletePlanDayCompletion(UUID userId, UUID planDayId) {
        completionRepository.deleteByUserIdAndTargetIdAndTypeAndPlanDayId(userId, planDayId, CompletionType.PLAN_DAY, planDayId);
    }

    @Cacheable(value = "userStats", key = "'workoutsThisWeek_' + #userId")
    public long getWorkoutsCompletedThisWeek(UUID userId) {
        LocalDateTime startOfWeek = LocalDateTime.of(LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1), LocalTime.MIN);
        return completionRepository.countByUserIdAndTypeAndCompletedAtAfter(userId, CompletionType.WORKOUT, startOfWeek);
    }

    @Cacheable(value = "userStats", key = "'totalMinutesThisWeek_' + #userId")
    @Transactional(readOnly = true)
    public long getTotalMinutesExercisedThisWeek(UUID userId) {
        LocalDateTime weekStart = LocalDateTime.now().minusWeeks(1);
        List<Completion> weeklyCompletions = completionRepository.findByUserIdAndTypeAndCompletedAtAfter(userId, CompletionType.WORKOUT, weekStart);
        long totalMinutes = 0;

        for (Completion completion : weeklyCompletions) {
            Workout workout = workoutRepository.findById(completion.getTargetId()).orElse(null);

            if (workout != null && workout.getWorkoutExercises() != null) {
                totalMinutes += workout.getWorkoutExercises().stream()
                    .filter(exercise -> exercise.getDuration() != null)
                    .mapToLong(WorkoutExercise::getDuration)
                    .sum();
            }
        }

        return totalMinutes;
    }

    @Transactional(readOnly = true)
    public long getExercisesCompletedThisWeek(UUID userId) {
        LocalDateTime weekStart = LocalDateTime.now().minusWeeks(1);

        List<Completion> weeklyCompletions = completionRepository.findByUserIdAndTypeAndCompletedAtAfter(userId, CompletionType.WORKOUT, weekStart);
        long totalExercises = 0;

        for (Completion completion : weeklyCompletions) {
            Workout workout = workoutRepository.findById(completion.getTargetId()).orElse(null);

            if (workout != null && workout.getWorkoutExercises() != null) {
                totalExercises += workout.getWorkoutExercises().size();
            }
        }

        return totalExercises;
    }

    @Cacheable(value = "userStats", key = "'caloriesToday_' + #userId")
    @Transactional(readOnly = true)
    public long getCaloriesBurnedToday(UUID userId) {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        List<Completion> todayCompletions = completionRepository.findByUserIdAndTypeAndCompletedAtAfter(userId, CompletionType.WORKOUT, startOfDay);
        long totalCaloriesBurned = 0;

        for (Completion completion : todayCompletions) {
            Workout workout = workoutRepository.findById(completion.getTargetId()).orElse(null);

            if (workout != null && workout.getWorkoutExercises() != null) {
                totalCaloriesBurned += workout.getWorkoutExercises().stream()
                    .filter(exercise -> exercise.getBurnedCalories() != null)
                    .mapToLong(exercise -> exercise.getBurnedCalories().longValue())
                    .sum();
            }
        }

        return totalCaloriesBurned;
    }

    @Cacheable(value = "userStats", key = "'totalWorkoutsCompleted_' + #userId")
    public long getTotalWorkoutsCompleted(UUID userId) {
        return completionRepository.countByUserIdAndType(userId, CompletionType.WORKOUT);
    }

    @Cacheable(value = "userStats", key = "'totalCaloriesBurned_' + #userId")
    @Transactional(readOnly = true)
    public long getTotalCaloriesBurned(UUID userId) {
        List<Completion> allCompletions = completionRepository.findByUserIdAndType(userId, CompletionType.WORKOUT);
        long totalCalories = 0;

        for (Completion completion : allCompletions) {
            Workout workout = workoutRepository.findById(completion.getTargetId()).orElse(null);

            if (workout != null && workout.getWorkoutExercises() != null) {
                totalCalories += workout.getWorkoutExercises().stream()
                        .filter(exercise -> exercise.getBurnedCalories() != null)
                        .mapToLong(exercise -> exercise.getBurnedCalories().longValue())
                        .sum();
            }
        }

        return totalCalories;
    }

    @Cacheable(value = "userStats", key = "'totalHoursExercised_' + #userId")
    @Transactional(readOnly = true)
    public double getTotalHoursExercised(UUID userId) {
        List<Completion> allCompletions = completionRepository.findByUserIdAndType(userId, CompletionType.WORKOUT);
        long totalSeconds = 0;

        for (Completion completion : allCompletions) {
            Workout workout = workoutRepository.findById(completion.getTargetId()).orElse(null);

            if (workout != null && workout.getWorkoutExercises() != null) {
                totalSeconds += workout.getWorkoutExercises().stream()
                        .mapToLong(FitnessMath::estimateDurationSeconds)
                        .sum();
            }
        }

        return Math.round((totalSeconds / 3600.0) * 10.0) / 10.0;
    }

    @Cacheable(value = "userStats", key = "'currentStreak_' + #userId")
    public long getCurrentDayStreak(UUID userId) {
        LocalDate currentDate = LocalDate.now();
        long streak = 0;

        while (true) {
            LocalDateTime startOfDay = LocalDateTime.of(currentDate.minusDays(streak), LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.of(currentDate.minusDays(streak), LocalTime.MAX);

            long completionsThatDay = completionRepository.countByUserIdAndCompletedAtBetween(userId, startOfDay, endOfDay);
            if (completionsThatDay == 0) {
                break;
            }
            streak++;
        }

        return streak;
    }

    public List<Completion> getRecentWorkoutCompletions(UUID userId, int limit) {
        return completionRepository.findByUserIdAndTypeOrderByCompletedAtDesc(userId, CompletionType.WORKOUT, limit);
    }

    @Transactional(readOnly = true)
    public List<RecentWorkoutDTO> getRecentWorkoutsWithDetails(UUID userId, int limit) {
        List<Completion> completions = getRecentWorkoutCompletions(userId, limit);
        List<RecentWorkoutDTO> recentWorkouts = new ArrayList<>();

        for (Completion completion : completions) {
            try {
                Workout workout = workoutRepository.findById(completion.getTargetId()).orElse(null);
                if (workout != null) {
                    RecentWorkoutDTO dto = new RecentWorkoutDTO();
                    dto.setWorkoutName(workout.getName());
                    dto.setWorkoutType(workout.getType() != null ? workout.getType().name() : "Unknown");

                    long actualCalories = 0;
                    long actualDuration = 0;

                    if (workout.getWorkoutExercises() != null) {
                        for (WorkoutExercise exercise : workout.getWorkoutExercises()) {
                            actualCalories += exercise.getBurnedCalories().longValue();
                            if (exercise.getDuration() != null) {
                                actualDuration += exercise.getDuration();
                            }
                        }
                    }

                    dto.setDurationMinutes((int) actualDuration);
                    dto.setCaloriesBurned((int) actualCalories);
                    dto.setCompletedAt(completion.getCompletedAt().toLocalDate());
                    recentWorkouts.add(dto);
                }
            } catch (Exception e) {

                continue;
            }
        }

        return recentWorkouts;
    }

    public long count() {
        return completionRepository.count();
    }


    public static class RecentWorkoutDTO {
        private String workoutName;
        private String workoutType;
        private int durationMinutes;
        private int caloriesBurned;
        private LocalDate completedAt;

        public RecentWorkoutDTO() {}

        public String getWorkoutName() { return workoutName; }
        public void setWorkoutName(String workoutName) { this.workoutName = workoutName; }

        public String getWorkoutType() { return workoutType; }
        public void setWorkoutType(String workoutType) { this.workoutType = workoutType; }

        public int getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

        public int getCaloriesBurned() { return caloriesBurned; }
        public void setCaloriesBurned(int caloriesBurned) { this.caloriesBurned = caloriesBurned; }

        public LocalDate getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDate completedAt) { this.completedAt = completedAt; }
    }
}

