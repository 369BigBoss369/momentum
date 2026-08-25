package com.momentum.fitness.service;

import com.momentum.fitness.model.Completion;
import com.momentum.fitness.model.PlanDay;
import com.momentum.fitness.model.Workout;
import com.momentum.fitness.model.WorkoutExercise;
import com.momentum.fitness.model.enums.CompletionType;
import com.momentum.fitness.model.enums.WorkoutType;
import com.momentum.fitness.repository.CompletionRepository;
import com.momentum.fitness.repository.WorkoutRepository;
import com.momentum.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompletionServiceUnitTest {

    @Mock
    private CompletionRepository completionRepository;

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private PlanService planService;

    @InjectMocks
    private CompletionService completionService;

    private User testUser;
    private UUID userId;
    private UUID planDayId;
    private UUID workoutId;
    private UUID exerciseId;
    private PlanDay planDay;
    private Workout workout;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        planDayId = UUID.randomUUID();
        workoutId = UUID.randomUUID();
        exerciseId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .build();

        planDay = PlanDay.builder()
                .id(planDayId)
                .build();

        workout = Workout.builder()
                .id(workoutId)
                .build();
    }

    @Test
    void markAsCompleted_ShouldCreateCompletionRecord() {
        Completion expectedCompletion = Completion.builder()
                .user(testUser)
                .targetId(workoutId)
                .type(CompletionType.WORKOUT)
                .planDayId(planDayId)
                .workoutPosition(0)
                .build();

        when(completionRepository.save(any(Completion.class))).thenReturn(expectedCompletion);

        Completion result = completionService.markAsCompleted(testUser, workoutId, CompletionType.WORKOUT.name(), planDayId, 0);

        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(workoutId, result.getTargetId());
        assertEquals(CompletionType.WORKOUT, result.getType());
        assertEquals(planDayId, result.getPlanDayId());
        assertEquals(0, result.getWorkoutPosition());
        verify(completionRepository).save(any(Completion.class));
    }

    @Test
    void getCompletionCountForUser_ShouldReturnCorrectCount() {
        when(completionRepository.countByUserId(userId)).thenReturn(5L);

        long result = completionService.getCompletionCountForUser(userId);

        assertEquals(5L, result);
        verify(completionRepository).countByUserId(userId);
    }

    @Test
    void getWorkoutsCompletedThisWeek_ShouldReturnCorrectCount() {
        when(completionRepository.countByUserIdAndTypeAndCompletedAtAfter(eq(userId), eq(CompletionType.WORKOUT), any(LocalDateTime.class))).thenReturn(3L);

        long result = completionService.getWorkoutsCompletedThisWeek(userId);

        assertEquals(3L, result);
        verify(completionRepository).countByUserIdAndTypeAndCompletedAtAfter(eq(userId), eq(CompletionType.WORKOUT), any(LocalDateTime.class));
    }

    @Test
    void getTotalMinutesExercisedThisWeek_ShouldReturnCorrectTotal() {
        Completion completion = Completion.builder()
                .targetId(UUID.randomUUID())
                .build();
        Workout workout = Workout.builder()
                .id(completion.getTargetId())
                .workoutExercises(List.of(
                        WorkoutExercise.builder().duration(30).build(),
                        WorkoutExercise.builder().duration(45).build()
                ))
                .build();

        when(completionRepository.findByUserIdAndTypeAndCompletedAtAfter(eq(userId), eq(CompletionType.WORKOUT), any(LocalDateTime.class)))
                .thenReturn(List.of(completion));
        when(workoutRepository.findById(completion.getTargetId())).thenReturn(Optional.of(workout));

        long result = completionService.getTotalMinutesExercisedThisWeek(userId);

        assertEquals(75L, result);
        verify(completionRepository).findByUserIdAndTypeAndCompletedAtAfter(eq(userId), eq(CompletionType.WORKOUT), any(LocalDateTime.class));
        verify(workoutRepository).findById(completion.getTargetId());
    }

    @Test
    void getCaloriesBurnedToday_ShouldReturnCorrectTotal() {
        Completion completion = Completion.builder()
                .targetId(UUID.randomUUID())
                .build();
        Workout workout = Workout.builder()
                .id(completion.getTargetId())
                .workoutExercises(List.of(
                        WorkoutExercise.builder().burnedCalories(250.0).build(),
                        WorkoutExercise.builder().burnedCalories(150.0).build()
                ))
                .build();

        when(completionRepository.findByUserIdAndTypeAndCompletedAtAfter(eq(userId), eq(CompletionType.WORKOUT), any(LocalDateTime.class)))
                .thenReturn(List.of(completion));
        when(workoutRepository.findById(completion.getTargetId())).thenReturn(Optional.of(workout));

        long result = completionService.getCaloriesBurnedToday(userId);

        assertEquals(400L, result);
        verify(completionRepository).findByUserIdAndTypeAndCompletedAtAfter(eq(userId), eq(CompletionType.WORKOUT), any(LocalDateTime.class));
        verify(workoutRepository).findById(completion.getTargetId());
    }

    @Test
    void getCurrentDayStreak_ShouldReturnCorrectStreak() {
        // Mock 3 consecutive days with completions
        when(completionRepository.countByUserIdAndCompletedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(1L)  // Today
                .thenReturn(1L)  // Yesterday
                .thenReturn(1L)  // Day before yesterday
                .thenReturn(0L); // No completion before that

        long result = completionService.getCurrentDayStreak(userId);

        assertEquals(3L, result);
        verify(completionRepository, times(4)).countByUserIdAndCompletedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void isPlanDayCompleted_ShouldReturnTrue_WhenCompleted() {
        when(completionRepository.existsByUserIdAndTargetIdAndTypeAndPlanDayIdAndWorkoutPosition(
                userId, planDayId, CompletionType.PLAN_DAY, planDayId, 0)).thenReturn(true);

        boolean result = completionService.isPlanDayCompleted(userId, planDay);

        assertTrue(result);
        verify(completionRepository).existsByUserIdAndTargetIdAndTypeAndPlanDayIdAndWorkoutPosition(
                userId, planDayId, CompletionType.PLAN_DAY, planDayId, 0);
    }

    @Test
    void isWorkoutCompleted_ShouldReturnTrue_WhenCompleted() {
        when(completionRepository.existsByUserIdAndTargetIdAndTypeAndPlanDayIdAndWorkoutPosition(
                userId, workoutId, CompletionType.WORKOUT, planDayId, 0)).thenReturn(true);

        boolean result = completionService.isWorkoutCompleted(userId, workoutId, planDayId, 0);

        assertTrue(result);
        verify(completionRepository).existsByUserIdAndTargetIdAndTypeAndPlanDayIdAndWorkoutPosition(
                userId, workoutId, CompletionType.WORKOUT, planDayId, 0);
    }

    @Test
    void isExerciseCompleted_ShouldReturnTrue_WhenCompleted() {
        when(completionRepository.existsByUserIdAndTargetIdAndTypeAndPlanDayIdAndWorkoutPosition(
                userId, exerciseId, CompletionType.EXERCISE, planDayId, 0)).thenReturn(true);

        boolean result = completionService.isExerciseCompleted(userId, exerciseId, planDayId, 0);

        assertTrue(result);
        verify(completionRepository).existsByUserIdAndTargetIdAndTypeAndPlanDayIdAndWorkoutPosition(
                userId, exerciseId, CompletionType.EXERCISE, planDayId, 0);
    }

    @Test
    void markPlanDayAsCompleted_ShouldCreateCompletion() {
        Completion expectedCompletion = Completion.builder()
                .user(testUser)
                .targetId(planDayId)
                .type(CompletionType.PLAN_DAY)
                .planDayId(planDayId)
                .workoutPosition(0)
                .build();

        when(completionRepository.save(any(Completion.class))).thenReturn(expectedCompletion);

        Completion result = completionService.markPlanDayAsCompleted(testUser, planDayId);

        assertNotNull(result);
        assertEquals(CompletionType.PLAN_DAY, result.getType());
        assertEquals(planDayId, result.getTargetId());
        verify(completionRepository).save(any(Completion.class));
    }

    @Test
    void markWorkoutAsCompleted_ShouldCreateCompletion() {
        Completion expectedCompletion = Completion.builder()
                .user(testUser)
                .targetId(workoutId)
                .type(CompletionType.WORKOUT)
                .planDayId(planDayId)
                .workoutPosition(1)
                .build();

        when(completionRepository.save(any(Completion.class))).thenReturn(expectedCompletion);

        Completion result = completionService.markWorkoutAsCompleted(testUser, workoutId, planDayId, 1);

        assertNotNull(result);
        assertEquals(CompletionType.WORKOUT, result.getType());
        assertEquals(workoutId, result.getTargetId());
        assertEquals(1, result.getWorkoutPosition());
        verify(completionRepository).save(any(Completion.class));
    }

    @Test
    void deleteCompletionsForWorkout_ShouldDeleteAllRelatedCompletions() {
        doNothing().when(completionRepository).deleteByUserIdAndTargetIdAndTypeAndPlanDayIdAndWorkoutPosition(
                userId, workoutId, CompletionType.WORKOUT, planDayId, 0);
        doNothing().when(completionRepository).deleteByUserIdAndTargetIdAndTypeAndPlanDayIdAndWorkoutPosition(
                userId, exerciseId, CompletionType.EXERCISE, planDayId, 0);

        completionService.deleteCompletionsForWorkout(userId, workoutId, planDayId, 0);

        verify(completionRepository).deleteByUserIdAndTargetIdAndTypeAndPlanDayIdAndWorkoutPosition(
                userId, workoutId, CompletionType.WORKOUT, planDayId, 0);
        verify(completionRepository).deleteByUserIdAndTargetIdAndTypeAndPlanDayIdAndWorkoutPosition(
                userId, exerciseId, CompletionType.EXERCISE, planDayId, 0);
    }

    @Test
    void getRecentCompletionsForUser_ShouldReturnOrderedList() {
        Completion completion1 = Completion.builder().completedAt(LocalDateTime.now().minusHours(1)).build();
        Completion completion2 = Completion.builder().completedAt(LocalDateTime.now().minusHours(2)).build();

        when(completionRepository.findByUserIdOrderByCompletedAtDesc(userId, 5))
                .thenReturn(Arrays.asList(completion1, completion2));

        List<Completion> result = completionService.getRecentCompletionsForUser(userId, 5);

        assertEquals(2, result.size());
        assertEquals(completion1, result.get(0));
        assertEquals(completion2, result.get(1));
        verify(completionRepository).findByUserIdOrderByCompletedAtDesc(userId, 5);
    }

    @Test
    void getRecentWorkoutsWithDetails_ShouldReturnWorkouts() {
        Completion completion = Completion.builder()
                .targetId(UUID.randomUUID())
                .completedAt(LocalDateTime.now())
                .build();
        Workout workout = Workout.builder()
                .id(completion.getTargetId())
                .name("Test Workout")
                .type(WorkoutType.STRENGTH)
                .build();

        when(completionRepository.findByUserIdAndTypeOrderByCompletedAtDesc(userId, CompletionType.WORKOUT, 5))
                .thenReturn(Arrays.asList(completion));
        when(workoutRepository.findById(completion.getTargetId())).thenReturn(Optional.of(workout));

        List<CompletionService.RecentWorkoutDTO> result = completionService.getRecentWorkoutsWithDetails(userId, 5);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Workout", result.get(0).getWorkoutName());
        verify(completionRepository).findByUserIdAndTypeOrderByCompletedAtDesc(userId, CompletionType.WORKOUT, 5);
        verify(workoutRepository).findById(completion.getTargetId());
    }
}
