package com.momentum.fitness.web.rest;

import com.momentum.fitness.dto.ExerciseSearchDTO;
import com.momentum.fitness.dto.ActivitySearchDTO;
import com.momentum.fitness.dto.WorkoutSearchDTO;
import com.momentum.fitness.model.Completion;
import com.momentum.fitness.model.Exercise;
import com.momentum.fitness.service.CompletionProcessingService;
import com.momentum.fitness.service.ExerciseService;
import com.momentum.fitness.service.FitnessSearchService;
import com.momentum.fitness.service.PlanService;
import com.momentum.fitness.service.PlanStartService;
import com.momentum.fitness.service.WorkoutService;
import com.momentum.user.service.FitnessActivityService;
import com.momentum.user.service.UserService;
import com.momentum.user.model.User;
import com.momentum.util.FitnessMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fitness")
public class FitnessRestController {
    private final ExerciseService exerciseService;
    private final WorkoutService workoutService;
    private final PlanService planService;
    private final FitnessSearchService fitnessSearchService;
    private final CompletionProcessingService completionProcessingService;
    private final PlanStartService planStartService;
    private final UserService userService;
    private final FitnessActivityService fitnessActivityService;

    public FitnessRestController(ExerciseService exerciseService, WorkoutService workoutService, PlanService planService, FitnessSearchService fitnessSearchService, CompletionProcessingService completionProcessingService, PlanStartService planStartService, UserService userService, FitnessActivityService fitnessActivityService) {
        this.exerciseService = exerciseService;
        this.workoutService = workoutService;
        this.planService = planService;
        this.fitnessSearchService = fitnessSearchService;
        this.completionProcessingService = completionProcessingService;
        this.planStartService = planStartService;
        this.userService = userService;
        this.fitnessActivityService = fitnessActivityService;
    }

    @GetMapping("/exercises")
    public List<ExerciseSearchDTO> searchExercises(@RequestParam String query, @RequestParam int limit, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        return FitnessMapper.mapToExerciseSearchDTO(exerciseService.searchByName(query, limit, user.getId()));
    }

    @GetMapping("/exercises/{id}")
    public ResponseEntity<Map<String, Object>> getExercise(@PathVariable UUID id, @AuthenticationPrincipal Object principal) {
        Exercise exercise = exerciseService.getAccessibleById(id, userService.getCurrentUser(principal));

        Map<String, Object> exerciseData = new HashMap<>();
        exerciseData.put("id", exercise.getId());
        exerciseData.put("name", exercise.getName());
        exerciseData.put("type", exercise.getType());
        exerciseData.put("imageUrl", exercise.getImageUrl());
        exerciseData.put("muscleGroupTarget", exercise.getMuscleGroupTarget());

        return ResponseEntity.ok(exerciseData);
    }

    @GetMapping("/workouts")
    public List<WorkoutSearchDTO> searchWorkouts(@RequestParam String query, @RequestParam int limit, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        return FitnessMapper.mapToWorkoutSearchDTO(workoutService.searchByName(query, limit, user.getId()));
    }

    @GetMapping("/activities")
    public ResponseEntity<List<ActivitySearchDTO>> search(@RequestParam(required = false) String query, @RequestParam(defaultValue = "ALL") String owner, @RequestParam(defaultValue = "EXERCISE") String type, @RequestParam(defaultValue = "ALL") String activityType, @RequestParam(required = false) Boolean inLibrary, @RequestParam(defaultValue = "50") int limit, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        return ResponseEntity.ok(fitnessSearchService.search(query, owner, type, activityType, inLibrary, limit, user.getId()));
    }

    @PatchMapping("/exercises/{exerciseId}/library")
    public ResponseEntity<?> addExerciseToLibrary(@PathVariable UUID exerciseId, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        exerciseService.addToLibrary(exerciseId, user.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/exercises/{exerciseId}/library")
    public ResponseEntity<?> removeExerciseFromLibrary(@PathVariable UUID exerciseId, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        exerciseService.removeFromLibrary(exerciseId, user.getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/workouts/{workoutId}/library")
    public ResponseEntity<?> addWorkoutToLibrary(@PathVariable UUID workoutId, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        workoutService.addToLibrary(workoutId, user.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/workouts/{workoutId}/library")
    public ResponseEntity<?> removeWorkoutFromLibrary(@PathVariable UUID workoutId, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        workoutService.removeFromLibrary(workoutId, user.getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/plans/{planId}/library")
    public ResponseEntity<?> addPlanToLibrary(@PathVariable UUID planId, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        planService.addToLibrary(planId, user.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/plans/{planId}/library")
    public ResponseEntity<?> removePlanFromLibrary(@PathVariable UUID planId, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        planService.removeFromLibrary(planId, user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/plans/{planId}/start")
    public ResponseEntity<?> startPlan(@PathVariable UUID planId, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        planStartService.startPlanForUser(planId, user.getId());
        fitnessActivityService.logPlanStarted(user.getId(), planService.getAccessibleById(planId).getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/completions")
    public ResponseEntity<Completion> markAsCompleted(@RequestParam String type, @RequestParam UUID targetId, @RequestParam UUID planDayId, @RequestParam Integer workoutPosition, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        Completion completion = completionProcessingService.processCompletionAndCascade(user, type, targetId, planDayId, workoutPosition);
        return ResponseEntity.ok(completion);
    }

    @DeleteMapping("/workouts/{workoutId}/restart")
    public ResponseEntity<?> restartWorkout(@PathVariable UUID workoutId, @RequestParam UUID planDayId, @RequestParam Integer workoutPosition, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        completionProcessingService.processWorkoutRestart(user, workoutId, planDayId, workoutPosition);
        return ResponseEntity.ok().build();
    }
}

