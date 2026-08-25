package com.momentum.fitness.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momentum.fitness.client.AIServiceClient;
import com.momentum.fitness.model.enums.MuscleType;
import feign.FeignException;
import com.momentum.fitness.dto.ai.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final AIServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;
    private final ExerciseService exerciseService;
    private final WorkoutService workoutService;

    public GenerateExerciseResponse generateExercise(String muscleGroup, String difficulty, String equipment, String userId) {
        try {
            log.info("Generating exercise with AI service. Request: GenerateExerciseRequest(muscleGroup={}, difficulty={}, equipment={}, userId={})", muscleGroup, difficulty, equipment, userId);

            String jsonResponse = aiServiceClient.generateExercise(muscleGroup, difficulty, equipment, userId);
            GenerateExerciseResponse response = objectMapper.readValue(jsonResponse, GenerateExerciseResponse.class);

            log.info("Successfully generated exercise: {}", response.getName());
            response.setSuccess(true);
            return response;
        } catch (feign.FeignException.ServiceUnavailable e) {
            log.error("AI service is currently unavailable", e);
            return GenerateExerciseResponse.builder()
                    .success(false)
                    .errorMessage("AI service is currently unavailable. Please try again later.")
                    .build();
        } catch (feign.FeignException.InternalServerError e) {
            log.error("AI service encountered an internal error", e);
            return GenerateExerciseResponse.builder()
                    .success(false)
                    .errorMessage("AI service is experiencing issues. Please try again in a few moments.")
                    .build();
        } catch (feign.FeignException e) {
            log.error("Feign error calling AI service", e);
            return GenerateExerciseResponse.builder()
                    .success(false)
                    .errorMessage("Connection to AI service failed. Please check if the AI service is running properly.")
                    .build();
        } catch (Exception e) {
            log.error("Failed to generate exercise with AI service", e);
            return GenerateExerciseResponse.builder()
                    .success(false)
                    .errorMessage("Unable to connect to AI service. Please check your connection and try again.")
                    .build();
        }
    }

    public GenerateWorkoutResponse generateWorkout(String type, String duration, String fitnessLevel, String goals, String userId, List<MuscleType> muscleGroups) {
        try {
            log.info("Generating workout with AI service. Request: GenerateWorkoutRequest(type={}, duration={}, fitnessLevel={}, goals={}, userId={}, muscleGroups={})", type, duration, fitnessLevel, goals, userId, muscleGroups);

            List<String> exerciseNames = exerciseService.getAvailableExerciseNamesByMuscleGroups(UUID.fromString(userId), muscleGroups);
            String availableExercises = exerciseNames.isEmpty()
                    ? "No existing exercises available - you can suggest exercises"
                    : "Available exercises: " + String.join(", ", exerciseNames);

            String jsonResponse = aiServiceClient.generateWorkout(type, duration, fitnessLevel, goals, userId, availableExercises);
            GenerateWorkoutResponse response = objectMapper.readValue(jsonResponse, GenerateWorkoutResponse.class);

            log.info("Successfully generated workout: {}", response.getName());
            response.setSuccess(true);
            return response;
        } catch (FeignException.ServiceUnavailable e) {
            log.error("AI service is currently unavailable", e);
            return GenerateWorkoutResponse.builder()
                    .success(false)
                    .errorMessage("AI service is currently unavailable. Please try again later.")
                    .build();
        } catch (FeignException.InternalServerError e) {
            log.error("AI service encountered an internal error", e);
            return GenerateWorkoutResponse.builder()
                    .success(false)
                    .errorMessage("AI service is experiencing issues. Please try again in a few moments.")
                    .build();
        } catch (Exception e) {
            log.error("Failed to generate workout with AI service", e);
            return GenerateWorkoutResponse.builder()
                    .success(false)
                    .errorMessage("Unable to connect to AI service. Please check your connection and try again.")
                    .build();
        }
    }

    public GeneratePlanResponse generatePlan(String duration, String frequency, String goals, String experience, String userId, List<MuscleType> muscleGroups) {
        try {
            log.info("Generating plan with AI service. Request: GeneratePlanRequest(duration={}, frequency={}, goals={}, experience={}, userId={}, muscleGroups={})", duration, frequency, goals, experience, userId, muscleGroups);

            List<String> workoutNames = workoutService.getAvailableWorkoutNamesByMuscleGroups(UUID.fromString(userId), muscleGroups);
            String availableWorkouts = workoutNames.isEmpty()
                    ? "No existing workouts available - you can suggest workouts"
                    : "Available workouts: " + String.join(", ", workoutNames);

            String jsonResponse = aiServiceClient.generatePlan(duration, frequency, goals, experience, userId, availableWorkouts);
            GeneratePlanResponse response = objectMapper.readValue(jsonResponse, GeneratePlanResponse.class);

            log.info("Successfully generated plan: {}", response.getName());
            response.setSuccess(true);
            return response;
        } catch (FeignException.ServiceUnavailable e) {
            log.error("AI service is currently unavailable", e);
            return GeneratePlanResponse.builder()
                    .success(false)
                    .errorMessage("AI service is currently unavailable. Please try again later.")
                    .build();
        } catch (FeignException.InternalServerError e) {
            log.error("AI service encountered an internal error", e);
            return GeneratePlanResponse.builder()
                    .success(false)
                    .errorMessage("AI service is experiencing issues. Please try again in a few moments.")
                    .build();
        } catch (FeignException e) {
            log.error("Feign error calling AI service", e);
            return GeneratePlanResponse.builder()
                    .success(false)
                    .errorMessage("Connection to AI service failed. Please check if the AI service is running properly.")
                    .build();
        } catch (Exception e) {
            log.error("Failed to generate plan with AI service", e);
            return GeneratePlanResponse.builder()
                    .success(false)
                    .errorMessage("Unable to connect to AI service. Please check your connection and try again.")
                    .build();
        }
    }
}

