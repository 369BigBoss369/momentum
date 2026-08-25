package com.momentum.fitness.service;

import com.momentum.fitness.dto.ai.GenerateExerciseResponse;
import com.momentum.fitness.dto.ai.GeneratePlanResponse;
import com.momentum.fitness.dto.ai.GenerateWorkoutResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.momentum.fitness.client.AIServiceClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIServiceUnitTest {

    @Mock
    private AIServiceClient aiServiceClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AIService aiService;

    @Test
    void generateExercise_ShouldReturnResponse_WhenSuccessful() throws Exception {
        GenerateExerciseResponse expectedResponse = GenerateExerciseResponse.builder()
                .success(true)
                .name("Push-up")
                .build();

        String jsonResponse = "{\"name\":\"Push-up\",\"success\":true}";

        when(aiServiceClient.generateExercise("chest", "beginner", "bodyweight", "user123"))
                .thenReturn(jsonResponse);
        when(objectMapper.readValue(jsonResponse, GenerateExerciseResponse.class))
                .thenReturn(expectedResponse);

        GenerateExerciseResponse result = aiService.generateExercise("chest", "beginner", "bodyweight", "user123");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Push-up", result.getName());
        verify(aiServiceClient).generateExercise("chest", "beginner", "bodyweight", "user123");
        verify(objectMapper).readValue(jsonResponse, GenerateExerciseResponse.class);
    }

    @Test
    void generateExercise_ShouldReturnErrorResponse_WhenApiFails() {
        when(aiServiceClient.generateExercise("chest", "beginner", "bodyweight", "user123"))
                .thenThrow(new RuntimeException("API Error"));

        GenerateExerciseResponse result = aiService.generateExercise("chest", "beginner", "bodyweight", "user123");

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Unable to connect"));
        verify(aiServiceClient).generateExercise("chest", "beginner", "bodyweight", "user123");
    }

    @Test
    void generateWorkout_ShouldReturnResponse_WhenSuccessful() throws Exception {
        GenerateWorkoutResponse expectedResponse = GenerateWorkoutResponse.builder()
                .success(true)
                .name("Upper Body Strength")
                .build();

        String jsonResponse = "{\"name\":\"Upper Body Strength\",\"success\":true}";

        when(aiServiceClient.generateWorkout("strength", "30", "intermediate", "build muscle", "user123", anyString()))
                .thenReturn(jsonResponse);
        when(objectMapper.readValue(jsonResponse, GenerateWorkoutResponse.class))
                .thenReturn(expectedResponse);

        GenerateWorkoutResponse result = aiService.generateWorkout("strength", "30", "intermediate", "build muscle", "user123", any());

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Upper Body Strength", result.getName());
        verify(aiServiceClient).generateWorkout("strength", "30", "intermediate", "build muscle", "user123", anyString());
        verify(objectMapper).readValue(jsonResponse, GenerateWorkoutResponse.class);
    }

    @Test
    void generateWorkout_ShouldReturnErrorResponse_WhenApiFails() {
        when(aiServiceClient.generateWorkout("strength", "30", "intermediate", "build muscle", "user123", anyString()))
                .thenThrow(new RuntimeException("API Error"));

        GenerateWorkoutResponse result = aiService.generateWorkout("strength", "30", "intermediate", "build muscle", "user123", any());

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Unable to connect"));
        verify(aiServiceClient).generateWorkout("strength", "30", "intermediate", "build muscle", "user123", anyString());
    }

    @Test
    void generatePlan_ShouldReturnResponse_WhenSuccessful() throws Exception {
        GeneratePlanResponse expectedResponse = GeneratePlanResponse.builder()
                .success(true)
                .name("8 Week Strength Program")
                .build();

        String jsonResponse = "{\"name\":\"8 Week Strength Program\",\"success\":true}";

        when(aiServiceClient.generatePlan("8", "3", "build muscle", "intermediate", "user123", anyString()))
                .thenReturn(jsonResponse);
        when(objectMapper.readValue(jsonResponse, GeneratePlanResponse.class))
                .thenReturn(expectedResponse);

        GeneratePlanResponse result = aiService.generatePlan("8", "3", "build muscle", "intermediate", "user123", any());

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("8 Week Strength Program", result.getName());
        verify(aiServiceClient).generatePlan("8", "3", "build muscle", "intermediate", "user123", anyString());
        verify(objectMapper).readValue(jsonResponse, GeneratePlanResponse.class);
    }

    @Test
    void generatePlan_ShouldReturnErrorResponse_WhenApiFails() {
        when(aiServiceClient.generatePlan("8", "3", "build muscle", "intermediate", "user123", anyString()))
                .thenThrow(new RuntimeException("API Error"));

        GeneratePlanResponse result = aiService.generatePlan("8", "3", "build muscle", "intermediate", "user123", any());

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Unable to connect"));
        verify(aiServiceClient).generatePlan("8", "3", "build muscle", "intermediate", "user123", anyString());
    }

    @Test
    void generateExercise_ShouldHandleNullParameters() throws Exception {
        GenerateExerciseResponse expectedResponse = GenerateExerciseResponse.builder()
                .success(true)
                .build();

        String jsonResponse = "{\"success\":true}";

        when(aiServiceClient.generateExercise(any(), any(), any(), any()))
                .thenReturn(jsonResponse);
        when(objectMapper.readValue(jsonResponse, GenerateExerciseResponse.class))
                .thenReturn(expectedResponse);

        GenerateExerciseResponse result = aiService.generateExercise(null, null, null, null);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(aiServiceClient).generateExercise(any(), any(), any(), any());
        verify(objectMapper).readValue(jsonResponse, GenerateExerciseResponse.class);
    }

    @Test
    void generateWorkout_ShouldHandleNullParameters() throws Exception {
        GenerateWorkoutResponse expectedResponse = GenerateWorkoutResponse.builder()
                .success(true)
                .build();

        String jsonResponse = "{\"success\":true}";

        when(aiServiceClient.generateWorkout(any(), any(), any(), any(), any(), any()))
                .thenReturn(jsonResponse);
        when(objectMapper.readValue(jsonResponse, GenerateWorkoutResponse.class))
                .thenReturn(expectedResponse);

        GenerateWorkoutResponse result = aiService.generateWorkout(null, null, null, null, null, null);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(aiServiceClient).generateWorkout(any(), any(), any(), any(), any(), any());
        verify(objectMapper).readValue(jsonResponse, GenerateWorkoutResponse.class);
    }

    @Test
    void generatePlan_ShouldHandleNullParameters() throws Exception {
        GeneratePlanResponse expectedResponse = GeneratePlanResponse.builder()
                .success(true)
                .build();

        String jsonResponse = "{\"success\":true}";

        when(aiServiceClient.generatePlan(any(), any(), any(), any(), any(), any()))
                .thenReturn(jsonResponse);
        when(objectMapper.readValue(jsonResponse, GeneratePlanResponse.class))
                .thenReturn(expectedResponse);

        GeneratePlanResponse result = aiService.generatePlan(null, null, null, null, null, null);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(aiServiceClient).generatePlan(any(), any(), any(), any(), any(), any());
        verify(objectMapper).readValue(jsonResponse, GeneratePlanResponse.class);
    }
}
