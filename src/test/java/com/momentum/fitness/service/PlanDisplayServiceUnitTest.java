package com.momentum.fitness.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.momentum.fitness.dto.PlanSummaryDTO;
import com.momentum.fitness.dto.TrackerDataDTO;
import com.momentum.fitness.model.Plan;
import com.momentum.fitness.model.PlanDay;
import com.momentum.fitness.model.enums.PlanDayType;
import com.momentum.user.model.User;
import com.momentum.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanDisplayServiceUnitTest {

    @Mock
    private PlanService planService;

    @Mock
    private CompletionService completionService;

    @Mock
    private UserService userService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PlanDisplayService planDisplayService;

    private UUID userId;
    private Plan testPlan;
    private TrackerDataDTO trackerData;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testPlan = Plan.builder()
                .id(UUID.randomUUID())
                .name("Test Plan")
                .build();

        PlanDay planDay = PlanDay.builder()
                .id(UUID.randomUUID())
                .dayNumber(1)
                .type(PlanDayType.ACTIVE)
                .build();

        testPlan.setPlanDays(Arrays.asList(planDay));

        trackerData = new TrackerDataDTO(testPlan, 1, 1, planDay);
    }

    @Test
    void preparePlanDisplayData_ShouldReturnDisplayData_WhenPlanExists() throws JsonProcessingException {
        when(planService.getTrackerDataForUser(userId)).thenReturn(trackerData);
        when(planService.getAccessibleByIdWithPlanDays(eq(testPlan.getId()), any(User.class))).thenReturn(testPlan);
        when(completionService.isPlanDayCompleted(userId, trackerData.getCurrentPlanDay().getId())).thenReturn(false);
        when(userService.getCurrentPlanStartDate(userId)).thenReturn(LocalDate.now());
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");

        PlanDisplayService.PlanDisplayData result = planDisplayService.preparePlanDisplayData(userId);

        assertNotNull(result);
        assertEquals(testPlan, result.getActivePlan());
        assertNotNull(result.getPlanDays());
        assertEquals(1, result.getPlanDays().size());
    }

    @Test
    void preparePlanDisplayData_ShouldReturnEmptyData_WhenNoPlan() throws JsonProcessingException {
        when(planService.getTrackerDataForUser(userId)).thenReturn(new TrackerDataDTO(null, 0, 0, null));
        when(planService.getPlanSummariesForUser(userId)).thenReturn(Arrays.asList(
            PlanSummaryDTO.builder().id(UUID.randomUUID()).name("Plan 1").totalWorkouts(5).build(),
            PlanSummaryDTO.builder().id(UUID.randomUUID()).name("Plan 2").totalWorkouts(3).build()
        ));

        PlanDisplayService.PlanDisplayData result = planDisplayService.preparePlanDisplayData(userId);

        assertNull(result.getActivePlan());
        assertEquals(2, result.getPlanSummaries().size());
    }

    @Test
    void preparePlanDisplayData_ShouldHandleJsonProcessingException() throws JsonProcessingException {
        when(planService.getTrackerDataForUser(userId)).thenReturn(trackerData);
        when(planService.getAccessibleByIdWithPlanDays(eq(testPlan.getId()), any(User.class))).thenReturn(testPlan);
        when(completionService.isPlanDayCompleted(userId, trackerData.getCurrentPlanDay().getId())).thenReturn(false);
        when(userService.getCurrentPlanStartDate(userId)).thenReturn(LocalDate.now());
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Test exception") {});

        assertThrows(JsonProcessingException.class, () -> planDisplayService.preparePlanDisplayData(userId));
    }
}
