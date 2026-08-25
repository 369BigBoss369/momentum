package com.momentum.fitness.web.rest;

import com.momentum.config.TestConfig;
import com.momentum.fitness.dto.ActivitySearchDTO;
import com.momentum.fitness.dto.ExerciseSearchDTO;
import com.momentum.fitness.dto.WorkoutSearchDTO;
import com.momentum.fitness.model.Exercise;
import com.momentum.fitness.service.CompletionProcessingService;
import com.momentum.fitness.service.ExerciseService;
import com.momentum.fitness.service.FitnessSearchService;
import com.momentum.fitness.service.PlanService;
import com.momentum.fitness.service.PlanStartService;
import com.momentum.fitness.service.WorkoutService;
import com.momentum.user.model.User;
import com.momentum.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FitnessRestController.class)
@Import(TestConfig.class)
class FitnessRestControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExerciseService exerciseService;

    @MockBean
    private WorkoutService workoutService;

    @MockBean
    private PlanService planService;

    @MockBean
    private FitnessSearchService fitnessSearchService;

    @MockBean
    private CompletionProcessingService completionProcessingService;

    @MockBean
    private PlanStartService planStartService;

    @MockBean
    private UserService userService;

    private UUID userId;
    private User mockUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = User.builder().id(userId).username("testuser").build();
        when(userService.getCurrentUser(any())).thenReturn(mockUser);
    }

    @Test
    @WithMockUser
    void searchExercises_ShouldReturnExerciseList() throws Exception {
        ExerciseSearchDTO exerciseDto = ExerciseSearchDTO.builder()
                .id(UUID.randomUUID())
                .name("Push-up")
                .build();

        when(exerciseService.searchByName(anyString(), anyInt(), any()))
                .thenReturn(Arrays.asList(new Exercise()));

        mockMvc.perform(get("/api/v1/fitness/exercises")
                        .param("query", "push")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser
    void getExercise_ShouldReturnExerciseData() throws Exception {
        UUID exerciseId = UUID.randomUUID();
        Exercise exercise = Exercise.builder()
                .id(exerciseId)
                .name("Push-up")
                .build();

        when(exerciseService.getAccessibleById(exerciseId, userService.getById(userId))).thenReturn(exercise);

        mockMvc.perform(get("/api/v1/fitness/exercises/{id}", exerciseId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser
    void searchWorkouts_ShouldReturnWorkoutList() throws Exception {
        WorkoutSearchDTO workoutDto = WorkoutSearchDTO.builder()
                .id(UUID.randomUUID())
                .name("Upper Body")
                .build();

        when(workoutService.searchByName(anyString(), anyInt(), any()))
                .thenReturn(Arrays.asList(new com.momentum.fitness.model.Workout()));

        mockMvc.perform(get("/api/v1/fitness/workouts")
                        .param("query", "upper")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser
    void searchActivities_ShouldReturnActivityList() throws Exception {
        ActivitySearchDTO activityDto = ActivitySearchDTO.builder()
                .id(UUID.randomUUID())
                .name("Push-up")
                .type("EXERCISE")
                .build();

        when(fitnessSearchService.search(any(), any(), any(), any(), any(), anyInt(), any()))
                .thenReturn(Arrays.asList(activityDto));

        mockMvc.perform(get("/api/v1/fitness/activities")
                        .param("query", "push")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser
    void addExerciseToLibrary_ShouldReturnOk() throws Exception {
        UUID exerciseId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/fitness/exercises/{exerciseId}/library", exerciseId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void removeExerciseFromLibrary_ShouldReturnOk() throws Exception {
        UUID exerciseId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/fitness/exercises/{exerciseId}/library", exerciseId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void addWorkoutToLibrary_ShouldReturnOk() throws Exception {
        UUID workoutId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/fitness/workouts/{workoutId}/library", workoutId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void removeWorkoutFromLibrary_ShouldReturnOk() throws Exception {
        UUID workoutId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/fitness/workouts/{workoutId}/library", workoutId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void addPlanToLibrary_ShouldReturnOk() throws Exception {
        UUID planId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/fitness/plans/{planId}/library", planId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void removePlanFromLibrary_ShouldReturnOk() throws Exception {
        UUID planId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/fitness/plans/{planId}/library", planId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void startPlan_ShouldReturnOk() throws Exception {
        UUID planId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/fitness/plans/{planId}/start", planId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void markAsCompleted_ShouldReturnCompletion() throws Exception {
        UUID targetId = UUID.randomUUID();
        UUID planDayId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/fitness/completions")
                        .param("type", "EXERCISE")
                        .param("targetId", targetId.toString())
                        .param("planDayId", planDayId.toString())
                        .param("workoutPosition", "0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser
    void restartWorkout_ShouldReturnOk() throws Exception {
        UUID workoutId = UUID.randomUUID();
        UUID planDayId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/fitness/workouts/{workoutId}/restart", workoutId)
                        .param("planDayId", planDayId.toString())
                        .param("workoutPosition", "0"))
                .andExpect(status().isOk());
    }
}

