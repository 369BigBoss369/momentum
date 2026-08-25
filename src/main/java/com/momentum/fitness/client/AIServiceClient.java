package com.momentum.fitness.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ai-service", url = "${ai.service.url}", configuration = com.momentum.config.FeignConfiguration.class)
public interface AIServiceClient {

    @PostMapping("/exercises")
    String generateExercise(
            @RequestParam("muscleGroup") String muscleGroup,
            @RequestParam("difficulty") String difficulty,
            @RequestParam("equipment") String equipment,
            @RequestParam("userId") String userId
    );

    @PostMapping("/workouts")
    String generateWorkout(
            @RequestParam("type") String type,
            @RequestParam("duration") String duration,
            @RequestParam("fitnessLevel") String fitnessLevel,
            @RequestParam("goals") String goals,
            @RequestParam("userId") String userId,
            @RequestParam("availableExercises") String availableExercises
    );

    @PostMapping("/plans")
    String generatePlan(
            @RequestParam("duration") String duration,
            @RequestParam("frequency") String frequency,
            @RequestParam("goals") String goals,
            @RequestParam("experience") String experience,
            @RequestParam("userId") String userId,
            @RequestParam("availableWorkouts") String availableWorkouts
    );
}

