package com.momentum.fitness.web;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.fitness.dto.*;
import com.momentum.fitness.dto.ai.*;
import com.momentum.fitness.model.*;
import com.momentum.fitness.model.enums.*;
import com.momentum.fitness.service.*;
import com.momentum.user.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.momentum.util.FitnessEditViewBuilder;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/fitness")
@Slf4j
public class FitnessController {
    private final ExerciseService exerciseService;
    private final WorkoutService workoutService;
    private final PlanService planService;
    private final UserService userService;
    private final CompletionService completionService;
    private final AIService aiService;
    private final ObjectMapper objectMapper;

    @Autowired
    public FitnessController(ExerciseService exerciseService, WorkoutService workoutService, PlanService planService, UserService userService, CompletionService completionService, AIService aiService, ObjectMapper objectMapper, AIService aiService1, ObjectMapper objectMapper1) {
        this.exerciseService = exerciseService;
        this.workoutService = workoutService;
        this.planService = planService;
        this.userService = userService;
        this.completionService = completionService;
        this.aiService = aiService1;
        this.objectMapper = objectMapper1;
    }

    @GetMapping("/dashboard")
    public ModelAndView fitnessDashboard(@AuthenticationPrincipal Object principal) {
        UUID userId = userService.extractUserId(principal);

        ModelAndView mv = new ModelAndView("fitness/dashboard");

        long workoutsThisWeek = completionService.getWorkoutsCompletedThisWeek(userId);
        long exercisesThisWeek = completionService.getExercisesCompletedThisWeek(userId);
        long caloriesToday = completionService.getCaloriesBurnedToday(userId);

        mv.addObject("workoutsThisWeek", workoutsThisWeek);
        mv.addObject("exercisesThisWeek", exercisesThisWeek);
        mv.addObject("caloriesToday", caloriesToday);

        List<com.momentum.fitness.service.CompletionService.RecentWorkoutDTO> recentWorkouts = completionService.getRecentWorkoutsWithDetails(userId, 5);
        mv.addObject("recentWorkouts", recentWorkouts);

        return mv;
    }

    @GetMapping("/ai-generate")
    public ModelAndView aiGenerate() {
        return new ModelAndView("fitness/ai-generate");
    }

    @PostMapping("/ai/generate-exercises")
    public ModelAndView generateExerciseWithAI(@AuthenticationPrincipal Object principal, @RequestParam("muscleGroup") String muscleGroup, @RequestParam("difficulty") String difficulty, @RequestParam("equipment") String equipment) throws JsonProcessingException {
        log.info("AI exercise generation requested - muscleGroup: {}, difficulty: {}, equipment: {}", muscleGroup, difficulty, equipment);
        UUID userId = userService.extractUserId(principal);
        GenerateExerciseResponse response = aiService.generateExercise(muscleGroup, difficulty, equipment, userId.toString());

        ModelAndView mv = new ModelAndView("fitness/ai-generate");

        if (!response.isSuccess()) {
            log.warn("AI exercise generation failed: {}", response.getErrorMessage());
            mv.addObject("aiError", response.getErrorMessage());
        } else {
            log.info("AI exercise generation successful: {}", response.getName());
            mv.addObject("generatedExercise", response);
            mv.addObject("showGeneratedExercise", true);

            if (response.getMuscleTargets() != null) {
                log.info("AI exercise has muscle targets: {}", response.getMuscleTargets());
                String json = objectMapper.writeValueAsString(response.getMuscleTargets());
                log.info("Serialized muscle targets JSON: {}", json);
                mv.addObject("muscleTargetsJson", json);
            } else {
                log.info("AI exercise has no muscle targets (null)");
                mv.addObject("muscleTargetsJson", "[]");
            }
        }
        return mv;
    }

    @PostMapping("/ai/generate-workouts")
    public ModelAndView generateWorkoutWithAI(@AuthenticationPrincipal Object principal, @RequestParam("type") String type, @RequestParam("duration") String duration, @RequestParam("fitnessLevel") String fitnessLevel, @RequestParam("goals") String goals, @RequestParam("muscleGroups") List<MuscleType> muscleGroups) throws JsonProcessingException {
        log.info("AI workout generation requested - type: {}, duration: {}, fitnessLevel: {}, goals: {}, muscleGroups: {}", type, duration, fitnessLevel, goals, muscleGroups);
        UUID userId = userService.extractUserId(principal);
        GenerateWorkoutResponse response = aiService.generateWorkout(type, duration, fitnessLevel, goals, userId.toString(), muscleGroups);

        ModelAndView mv = new ModelAndView("fitness/ai-generate");

        if (!response.isSuccess()) {
            log.warn("AI workout generation failed: {}", response.getErrorMessage());
            mv.addObject("aiError", response.getErrorMessage());
            mv.addObject("showGeneratedWorkout", false);
        } else {
            log.info("AI workout generation successful: {} - type: {}", response.getName(), response.getType());
            log.info("Workout exercises count: {}", response.getWorkoutExercises() != null ? response.getWorkoutExercises().size() : 0);
            mv.addObject("generatedWorkout", response);
            mv.addObject("showGeneratedWorkout", true);

            if (response.getWorkoutExercises() != null) {
                String json = objectMapper.writeValueAsString(response.getWorkoutExercises());
                mv.addObject("workoutExercisesJson", json);
                log.info("Serialized workout exercises JSON length: {}", json.length());
            } else {
                mv.addObject("workoutExercisesJson", "[]");
                log.info("No workout exercises to serialize");
            }

            log.info("Set showGeneratedWorkout to true");
        }
        return mv;
    }

    @PostMapping("/ai/generate-plans")
    public ModelAndView generatePlanWithAI(@AuthenticationPrincipal Object principal, @RequestParam("duration") String duration, @RequestParam("frequency") String frequency, @RequestParam("goals") String goals, @RequestParam("experience") String experience, @RequestParam("muscleGroups") List<MuscleType> muscleGroups) throws JsonProcessingException {
        UUID userId = userService.extractUserId(principal);
        GeneratePlanResponse response = aiService.generatePlan(duration, frequency, goals, experience, userId.toString(), muscleGroups);

        ModelAndView mv = new ModelAndView("fitness/ai-generate");
        mv.addObject("selectedMuscleGroups", muscleGroups);

        if (!response.isSuccess()) {
            log.warn("AI plan generation failed: {}", response.getErrorMessage());
            mv.addObject("aiError", response.getErrorMessage());
            mv.addObject("showGeneratedPlan", false);
        } else {
            log.info("AI plan generation successful: {} - type: {}", response.getName(), response.getType());
            log.info("Plan days count: {}", response.getPlanDays() != null ? response.getPlanDays().size() : 0);
            mv.addObject("generatedPlan", response);
            mv.addObject("showGeneratedPlan", true);

            if (response.getPlanDays() != null) {
                String json = objectMapper.writeValueAsString(response.getPlanDays());
                mv.addObject("planDaysJson", json);
                log.info("Serialized plan days JSON length: {}", json.length());
            } else {
                mv.addObject("planDaysJson", "[]");
                log.info("No plan days to serialize");
            }

            log.info("Set showGeneratedPlan to true");
        }
        return mv;
    }

    @PostMapping("/ai/use-exercise")
    public ModelAndView useAIExercise(@RequestParam("name") String name, @RequestParam("type") String type, @RequestParam(value = "muscleTargetsJson", required = false) String muscleTargetsJson, @RequestParam(value = "imageUrl", required = false) String imageUrl, @RequestParam(value = "videoUrl", required = false) String videoUrl, @AuthenticationPrincipal Object principal) throws JsonProcessingException {
        CreateExerciseDTO createExerciseDTO = new CreateExerciseDTO();
        createExerciseDTO.setName(name);
        createExerciseDTO.setType(ExerciseType.valueOf(type.toUpperCase()));

        log.info("Received muscleTargetsJson: {}", muscleTargetsJson);
        List<MuscleTarget> muscleTargets = objectMapper.readValue(muscleTargetsJson, objectMapper.getTypeFactory().constructCollectionType(List.class, MuscleTarget.class));
        log.info("Parsed muscle targets: {}", muscleTargets);
        createExerciseDTO.setMuscleTargets(muscleTargets);

        createExerciseDTO.setImageUrl(imageUrl);
        createExerciseDTO.setVideoUrl(videoUrl);

        ModelAndView mv = new ModelAndView("fitness/create-workout");
        mv.addObject("createExerciseDTO", createExerciseDTO);
        mv.addObject("createWorkoutDTO", new CreateWorkoutDTO());
        mv.addObject("createPlanDTO", new CreatePlanDTO());
        mv.addObject("editMode", false);
        mv.addObject("editType", null);
        mv.addObject("editId", null);
        mv.addObject("activeTab", "exercise");

        mv.addObject("simplifiedMuscleTargets", createExerciseDTO.getMuscleTargets() != null ? createExerciseDTO.getMuscleTargets() : new ArrayList<>());

        return mv;
    }

    @PostMapping("/ai/use-workout")
    public String useAIWorkout(@RequestParam("workoutName") String workoutName, @RequestParam("workoutType") String workoutType, @RequestParam(value = "workoutExercisesJson", required = false) String workoutExercisesJson, @AuthenticationPrincipal Object principal, RedirectAttributes redirectAttributes) throws JsonProcessingException {
        UUID userId = userService.extractUserId(principal);

        List<GenerateWorkoutResponse.AIGeneratedWorkoutExercise> aiExercises =
                objectMapper.readValue(workoutExercisesJson, objectMapper.getTypeFactory().constructCollectionType(List.class, GenerateWorkoutResponse.AIGeneratedWorkoutExercise.class));

        List<CreateWorkoutExerciseDTO> exerciseDTOs = new ArrayList<>();
        List<String> notFound = new ArrayList<>();
        int position = 1;

        for (GenerateWorkoutResponse.AIGeneratedWorkoutExercise aiExercise : aiExercises) {
            Optional<Exercise> match = exerciseService.findAccessibleByName(userId, aiExercise.getExerciseName());
            if (match.isEmpty()) {
                notFound.add(aiExercise.getExerciseName());
                continue;
            }

            Exercise exercise = match.get();
            exerciseDTOs.add(CreateWorkoutExerciseDTO.builder()
                    .exerciseId(exercise.getId())
                    .type(exercise.getType())
                    .number(position++)
                    .reps(aiExercise.getReps())
                    .weight(aiExercise.getWeight())
                    .duration(aiExercise.getDuration())
                    .build());
        }

        if (!notFound.isEmpty()) {
            log.warn("Could not match {} AI-suggested exercise(s) to accessible exercises: {}", notFound.size(), notFound);
        }

        if (exerciseDTOs.isEmpty()) {
            redirectAttributes.addFlashAttribute("aiError", "Could not create the workout - none of the suggested exercises could be matched to your available exercises.");
            return "redirect:/fitness/ai-generate";
        }

        CreateWorkoutDTO createWorkoutDTO = CreateWorkoutDTO.builder()
                .name(workoutName)
                .type(WorkoutType.valueOf(workoutType.toUpperCase()))
                .exercises(exerciseDTOs)
                .build();

        Workout savedWorkout = workoutService.createWorkout(createWorkoutDTO, userId);
        log.info("Created workout '{}' (ID: {}) from AI suggestion with {} exercises ({} skipped)", savedWorkout.getName(), savedWorkout.getId(), exerciseDTOs.size(), notFound.size());

        redirectAttributes.addFlashAttribute("successMessage", "Workout created successfully" + (notFound.isEmpty() ? "!" : " (" + notFound.size() + " suggested exercise(s) could not be matched and were skipped)"));
        return "redirect:/fitness/workouts/" + savedWorkout.getId();
    }

    @PostMapping("/ai/use-plan")
    public String useAIPlan(@RequestParam("planName") String planName, @RequestParam("planDescription") String planDescription, @RequestParam("planType") String planType, @RequestParam(value = "planDaysJson", required = false) String planDaysJson, @AuthenticationPrincipal Object principal, RedirectAttributes redirectAttributes) throws JsonProcessingException {
        UUID userId = userService.extractUserId(principal);

        List<GeneratePlanResponse.AIGeneratedPlanDay> aiPlanDays =
                objectMapper.readValue(planDaysJson, objectMapper.getTypeFactory().constructCollectionType(List.class, GeneratePlanResponse.AIGeneratedPlanDay.class));

        List<CreatePlanDayDTO> dayDTOs = new ArrayList<>();
        List<String> notFound = new ArrayList<>();

        for (GeneratePlanResponse.AIGeneratedPlanDay aiDay : aiPlanDays) {
            List<UUID> workoutIds = new ArrayList<>();

            if (aiDay.getWorkoutNames() != null) {
                for (String workoutName : aiDay.getWorkoutNames()) {
                    Optional<Workout> match = workoutService.findAccessibleByName(userId, workoutName);
                    if (match.isEmpty()) {
                        notFound.add(workoutName);
                        continue;
                    }
                    workoutIds.add(match.get().getId());
                }
            }

            dayDTOs.add(CreatePlanDayDTO.builder()
                    .dayNumber(aiDay.getDayNumber())
                    .type(PlanDayType.valueOf(aiDay.getType().toUpperCase()))
                    .workoutIds(workoutIds)
                    .build());
        }

        if (!notFound.isEmpty()) {
            log.warn("Could not match {} AI-suggested workout(s) to accessible workouts: {}", notFound.size(), notFound);
        }

        CreatePlanDTO createPlanDTO = CreatePlanDTO.builder()
                .name(planName)
                .description(planDescription)
                .type(PlanType.valueOf(planType.toUpperCase()))
                .days(dayDTOs)
                .build();

        Plan savedPlan = planService.createPlan(createPlanDTO, userId);
        log.info("Created plan '{}' (ID: {}) from AI suggestion with {} days ({} workout(s) skipped)", savedPlan.getName(), savedPlan.getId(), dayDTOs.size(), notFound.size());

        redirectAttributes.addFlashAttribute("successMessage", "Plan created successfully" + (notFound.isEmpty() ? "!" : " (" + notFound.size() + " suggested workout(s) could not be matched and were skipped)"));
        return "redirect:/fitness/plans/" + savedPlan.getId();
    }

    @GetMapping("/create-activity")
    public ModelAndView createActivity() {
        ModelAndView mv = new ModelAndView("fitness/create-workout");

        mv.addObject("createExerciseDTO", new CreateExerciseDTO());
        mv.addObject("createWorkoutDTO", new CreateWorkoutDTO());
        mv.addObject("createPlanDTO", new CreatePlanDTO());
        mv.addObject("editMode", false);
        mv.addObject("editType", null);
        mv.addObject("editId", null);
        mv.addObject("activeTab", "exercise");

        return mv;
    }

    @GetMapping("/tracker")
    public ModelAndView workoutTracker(@AuthenticationPrincipal Object principal) {
        UUID userId = userService.extractUserId(principal);
        TrackerDataDTO trackerData = planService.getTrackerDataForUser(userId);
        Plan activePlan = trackerData.getActivePlan();

        return planService.getTrackerPageModelAndView(activePlan, trackerData, userId);
    }

    @GetMapping("/plans")
    public ModelAndView workoutPlans(@AuthenticationPrincipal Object principal) throws JsonProcessingException {
        UUID userId = userService.extractUserId(principal);
        TrackerDataDTO trackerData = planService.getTrackerDataForUser(userId);
        Plan activePlan = trackerData.getActivePlan();

        return planService.getPlansPageModelAndView(activePlan, userId, trackerData);
    }

    @GetMapping("/exercises")
    public String exercises() {
        return "fitness/exercises";
    }

    @GetMapping("/exercises/{id}")
    public ModelAndView viewExercise(@PathVariable UUID id, @AuthenticationPrincipal Object principal) {
        UUID userId = userService.extractUserId(principal);
        Exercise exercise = exerciseService.getAccessibleById(id, userService.getCurrentUser(principal));

        ModelAndView mv = new ModelAndView("fitness/exercise-view");
        mv.addObject("exercise", exercise);
        mv.addObject("isOwner", exerciseService.isOwner(exercise, userId));
        mv.addObject("isInLibrary", exerciseService.isInLibrary(id, userId));

        return mv;
    }

    @GetMapping("/workouts/{id}")
    public ModelAndView viewWorkout(@PathVariable UUID id, @AuthenticationPrincipal Object principal) {
        UUID userId = userService.extractUserId(principal);
        Workout workout = workoutService.getAccessibleById(id, userService.getCurrentUser(principal));

        ModelAndView mv = new ModelAndView("fitness/workout-view");
        mv.addObject("workout", workout);
        mv.addObject("isOwner", workoutService.isOwner(workout, userId));
        mv.addObject("isInLibrary", workoutService.isInLibrary(id, userId));

        return mv;
    }

    @GetMapping("/plans/{id}")
    public ModelAndView viewPlan(@PathVariable UUID id, @AuthenticationPrincipal Object principal) {
        UUID userId = userService.extractUserId(principal);
        Plan plan = planService.getAccessibleByIdWithPlanDays(id, userService.getById(userId));

        ModelAndView mv = new ModelAndView("fitness/plan-view");
        mv.addObject("plan", plan);
        mv.addObject("isOwner", planService.isOwner(plan, userId));
        mv.addObject("isInLibrary", planService.isInLibrary(id, userId));
        mv.addObject("totalWorkouts", plan.getPlanDays().stream()
                .mapToInt(day -> day.getWorkouts().size())
                .sum());

        return mv;
    }

    @GetMapping("/exercises/{id}/edit")
    public ModelAndView editExercise(@PathVariable UUID id, @AuthenticationPrincipal Object principal) {
        return FitnessEditViewBuilder.build(
                exerciseService.getByIdWithMuscleTargets(id, userService.extractUserId(principal)),
                id,
                "EXERCISE"
        );
    }

    @GetMapping("/workouts/{id}/edit")
    public ModelAndView editWorkout(@PathVariable UUID id, @AuthenticationPrincipal Object principal) {
        Workout workout = workoutService.getEditable(id, userService.extractUserId(principal));

        List<WorkoutExercise> exercises = workoutService.getExercisesByWorkoutId(id);
        workout.setWorkoutExercises(exercises);

        return FitnessEditViewBuilder.build(workout, id, "WORKOUT");
    }

    @GetMapping("/plans/{id}/edit")
    public ModelAndView editPlan(@PathVariable UUID id, @AuthenticationPrincipal Object principal) {
        return FitnessEditViewBuilder.build(planService.getEditable(id, userService.extractUserId(principal)),
                id,
                "PLAN"
        );
    }

    @GetMapping("/activity-created-success")
    public ModelAndView activityCreatedSuccess(@RequestParam("type") String type,
                                               @RequestParam(value = "action", required = false) String action,
                                               @RequestParam(value = "pending", required = false, defaultValue = "false") boolean pending) {
        ModelAndView mv = new ModelAndView("fitness/activity-created-success");

        mv.addObject("activityType", type);
        mv.addObject("action", action);
        mv.addObject("pending", pending);

        return mv;
    }

    @PostMapping("/create/exercise")
    public ModelAndView createExercise(@AuthenticationPrincipal Object principal, @Valid CreateExerciseDTO createExerciseDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("fitness/create-workout", "activeTab", "exercise");
        }

        Exercise savedExercise = exerciseService.createExercise(createExerciseDto, userService.extractUserId(principal));
        boolean pending = savedExercise.getModerationStatus() == ModerationStatus.PENDING;

        return new ModelAndView("redirect:/fitness/activity-created-success?type=exercise&pending=" + pending);
    }

    @PutMapping("/exercises/{id}")
    public ModelAndView updateExercise(@PathVariable UUID id, @AuthenticationPrincipal Object principal, @Valid CreateExerciseDTO updateDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return FitnessEditViewBuilder.build(exerciseService.getById(id), id, "EXERCISE");
        }

        Exercise savedExercise = exerciseService.updateExercise(id, updateDto, userService.extractUserId(principal));
        boolean pending = savedExercise.getModerationStatus() == ModerationStatus.PENDING;

        return new ModelAndView("redirect:/fitness/activity-created-success?type=exercise&action=updated&pending=" + pending);
    }

    @PostMapping("/create/workout")
    public ModelAndView createWorkout(@AuthenticationPrincipal Object principal, @Valid CreateWorkoutDTO createWorkoutDto, BindingResult bindingResult) {
        log.info("Creating workout - principal: {}, dto: {}", principal, createWorkoutDto);

        if (bindingResult.hasErrors()) {
            log.warn("Workout creation failed - validation errors: {}", bindingResult.getAllErrors());
            return new ModelAndView("fitness/create-workout", "activeTab", "workout");
        }

        try {
            UUID userId = userService.extractUserId(principal);
            log.info("Creating workout for user: {}", userId);

            Workout savedWorkout = workoutService.createWorkout(createWorkoutDto, userId);
            log.info("Workout created successfully");
            boolean pending = savedWorkout.getModerationStatus() == ModerationStatus.PENDING;

            return new ModelAndView("redirect:/fitness/activity-created-success?type=workout&pending=" + pending);
        } catch (Exception e) {
            log.error("Failed to create workout", e);
            ModelAndView mv = new ModelAndView("fitness/create-workout", "activeTab", "workout");
            mv.addObject("error", "Failed to create workout: " + e.getMessage());
            return mv;
        }
    }

    @PutMapping("/workouts/{id}")
    public ModelAndView updateWorkout(@PathVariable UUID id, @AuthenticationPrincipal Object principal, @Valid CreateWorkoutDTO workoutDto, BindingResult bindingResult) {
        UUID userId = userService.extractUserId(principal);
        Workout workout = workoutService.getAccessibleById(id, userService.getCurrentUser(principal));

        if (bindingResult.hasErrors()) {
            List<WorkoutExercise> exercises = workoutService.getExercisesByWorkoutId(id);
            workout.setWorkoutExercises(exercises);

            return FitnessEditViewBuilder.build(workout, id, "WORKOUT");
        }

        Workout savedWorkout = workoutService.updateWorkout(id, workoutDto, userId);
        boolean pending = savedWorkout.getModerationStatus() == ModerationStatus.PENDING;

        return new ModelAndView("redirect:/fitness/activity-created-success?type=workout&action=updated&pending=" + pending);
    }

    @PostMapping("/create/plan")
    public ModelAndView createPlan(@AuthenticationPrincipal Object principal, @Valid CreatePlanDTO planDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("fitness/create-workout", "activeTab", "plan");
        }

        Plan savedPlan = planService.createPlan(planDTO, userService.extractUserId(principal));
        boolean pending = savedPlan.getModerationStatus() == ModerationStatus.PENDING;

        return new ModelAndView("redirect:/fitness/activity-created-success?type=plan&pending=" + pending);
    }

    @PutMapping("/plans/{id}")
    public ModelAndView updatePlan(@PathVariable UUID id, @AuthenticationPrincipal Object principal, @Valid CreatePlanDTO planDTO, BindingResult bindingResult) {
        ModelAndView mv = new ModelAndView("fitness/create-workout");

        if (bindingResult.hasErrors()) {
            mv.addObject("activeTab", "plan");
            return mv;
        }

        Plan savedPlan = planService.updatePlan(id, planDTO, userService.extractUserId(principal));
        boolean pending = savedPlan.getModerationStatus() == ModerationStatus.PENDING;

        return new ModelAndView("redirect:/fitness/activity-created-success?type=plan&action=updated&pending=" + pending);
    }
}