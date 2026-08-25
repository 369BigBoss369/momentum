package com.momentum.fitness.service;

import com.momentum.fitness.dto.ActivitySearchDTO;
import com.momentum.fitness.model.Exercise;
import com.momentum.fitness.model.Workout;
import com.momentum.fitness.model.Plan;
import com.momentum.fitness.model.enums.PlanType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FitnessSearchServiceUnitTest {

    @Mock
    private ExerciseService exerciseService;

    @Mock
    private WorkoutService workoutService;

    @Mock
    private PlanService planService;

    @InjectMocks
    private FitnessSearchService fitnessSearchService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void search_ShouldReturnCombinedResults_WhenQueryProvided() {
        Exercise exercise = Exercise.builder()
                .id(UUID.randomUUID())
                .name("Push-up")
                .build();

        Workout workout = Workout.builder()
                .id(UUID.randomUUID())
                .name("Upper Body")
                .build();

        Plan plan = Plan.builder()
                .id(UUID.randomUUID())
                .name("Strength Plan")
                .build();

        when(exerciseService.searchByName("push", 50, userId)).thenReturn(Arrays.asList(exercise));
        when(workoutService.searchByName("push", 50, userId)).thenReturn(Arrays.asList(workout));
        when(planService.search(eq("push"), isNull(), eq(userId), any(Pageable.class))).thenReturn(Page.empty());

        List<ActivitySearchDTO> result = fitnessSearchService.search("push", null, "ALL", "ALL", null, 50, userId);

        assertNotNull(result);
        assertTrue(result.size() >= 2); // Should have exercise and workout results
        verify(exerciseService).searchByName("push", 50, userId);
        verify(workoutService).searchByName("push", 50, userId);
        verify(planService).search(eq("push"), isNull(), eq(userId), any(Pageable.class));
    }

    @Test
    void search_ShouldFilterByType_WhenExerciseTypeSpecified() {
        Exercise exercise = Exercise.builder()
                .id(UUID.randomUUID())
                .name("Push-up")
                .build();

        when(exerciseService.searchByName("push", 50, userId)).thenReturn(Arrays.asList(exercise));

        List<ActivitySearchDTO> result = fitnessSearchService.search("push", "OWNED", "EXERCISE", "ALL", null, 50, userId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(exerciseService).searchByName("push", 50, userId);
        verify(workoutService, never()).searchByName(anyString(), anyInt(), any());
        verify(planService, never()).search(anyString(), any(), any(), any(Pageable.class));
    }

    @Test
    void search_ShouldFilterByType_WhenWorkoutTypeSpecified() {
        Workout workout = Workout.builder()
                .id(UUID.randomUUID())
                .name("Upper Body")
                .build();

        when(workoutService.searchByName("upper", 50, userId)).thenReturn(Arrays.asList(workout));

        List<ActivitySearchDTO> result = fitnessSearchService.search("upper", "OWNED", "WORKOUT", "ALL", null, 50, userId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(workoutService).searchByName("upper", 50, userId);
        verify(exerciseService, never()).searchByName(anyString(), anyInt(), any());
        verify(planService, never()).search(anyString(), any(), any(), any(Pageable.class));
    }

    @Test
    void search_ShouldFilterByType_WhenPlanTypeSpecified() {
        Plan plan = Plan.builder()
                .id(UUID.randomUUID())
                .name("Strength Plan")
                .build();

        when(planService.search(eq("strength"), isNull(), eq(userId), any(Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(Arrays.asList(plan)));

        List<ActivitySearchDTO> result = fitnessSearchService.search("strength", "OWNED", "PLAN", "ALL", null, 50, userId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(planService).search(eq("strength"), isNull(), eq(userId), any(Pageable.class));
        verify(exerciseService, never()).searchByName(anyString(), anyInt(), any());
        verify(workoutService, never()).searchByName(anyString(), anyInt(), any());
    }

    @Test
    void search_ShouldHandleNullQuery() {
        when(exerciseService.searchByName("", 50, userId)).thenReturn(Arrays.asList());
        when(workoutService.searchByName("", 50, userId)).thenReturn(Arrays.asList());
        when(planService.search(isNull(), isNull(), eq(userId), any(Pageable.class))).thenReturn(Page.empty());

        List<ActivitySearchDTO> result = fitnessSearchService.search(null, null, "ALL", "ALL", null, 50, userId);

        assertNotNull(result);
        verify(exerciseService).searchByName("", 50, userId);
        verify(workoutService).searchByName("", 50, userId);
        verify(planService).search(isNull(), isNull(), eq(userId), any(Pageable.class));
    }

    @Test
    void search_ShouldLimitResults() {
        Exercise exercise = Exercise.builder()
                .id(UUID.randomUUID())
                .name("Push-up")
                .build();

        Workout workout = Workout.builder()
                .id(UUID.randomUUID())
                .name("Upper Body")
                .build();

        when(exerciseService.searchByName("test", 10, userId)).thenReturn(Arrays.asList(exercise));
        when(workoutService.searchByName("test", 10, userId)).thenReturn(Arrays.asList(workout));
        when(planService.search(eq("test"), isNull(), eq(userId), any(Pageable.class))).thenReturn(Page.empty());

        List<ActivitySearchDTO> result = fitnessSearchService.search("test", null, "ALL", "ALL", null, 10, userId);

        assertNotNull(result);
        verify(exerciseService).searchByName("test", 10, userId);
        verify(workoutService).searchByName("test", 10, userId);
        verify(planService).search(eq("test"), isNull(), eq(userId), any(Pageable.class));
    }

    @Test
    void search_ShouldHandleEmptyResults() {
        when(exerciseService.searchByName("nonexistent", 50, userId)).thenReturn(Arrays.asList());
        when(workoutService.searchByName("nonexistent", 50, userId)).thenReturn(Arrays.asList());
        when(planService.search(eq("nonexistent"), isNull(), eq(userId), any(Pageable.class))).thenReturn(Page.empty());

        List<ActivitySearchDTO> result = fitnessSearchService.search("nonexistent", null, "ALL", "ALL", null, 50, userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void search_ShouldPassParametersCorrectly() {
        when(exerciseService.searchByName("query", 25, userId)).thenReturn(Arrays.asList());
        when(workoutService.searchByName("query", 25, userId)).thenReturn(Arrays.asList());
        when(planService.search(eq("query"), eq(PlanType.STRENGTH), eq(userId), any(Pageable.class))).thenReturn(Page.empty());

        List<ActivitySearchDTO> result = fitnessSearchService.search("query", "PUBLIC", "PLAN", "STRENGTH", true, 25, userId);

        assertNotNull(result);
        verify(exerciseService).searchByName("query", 25, userId);
        verify(workoutService).searchByName("query", 25, userId);
        verify(planService).search(eq("query"), eq(PlanType.STRENGTH), eq(userId), any(Pageable.class));
    }
}
