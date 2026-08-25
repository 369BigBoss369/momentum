package com.momentum.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momentum.fitness.dto.*;
import com.momentum.fitness.model.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.stream.Collectors;

public class FitnessEditViewBuilder {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ModelAndView build(Object entity, UUID id, String editType) {
        ModelAndView mv = new ModelAndView("fitness/create-workout");

        switch (editType) {
            case "EXERCISE" -> buildExerciseView((Exercise) entity, mv);
            case "WORKOUT"  -> buildWorkoutView((Workout) entity, mv);
            case "PLAN"     -> buildPlanView((Plan) entity, mv);
            default -> throw new IllegalArgumentException("Unknown edit type: " + editType);
        }

        mv.addObject("editMode", true);
        mv.addObject("editType", editType);
        mv.addObject("editId", id);
        mv.addObject("activeTab", editType.toLowerCase()
        );

        return mv;
    }

    private static void buildExerciseView(Exercise exercise, ModelAndView mv) {
        CreateExerciseDTO dto = CreateExerciseDTO.builder()
                .name(exercise.getName())
                .type(exercise.getType())
                .muscleTargets(exercise.getMuscleGroupTarget())
                .imageUrl(exercise.getImageUrl())
                .videoUrl(exercise.getVideoUrl())
                .isPublic(exercise.getIsPublic())
                .build();

        mv.addObject("createExerciseDTO", dto);
        mv.addObject("createWorkoutDTO", new CreateWorkoutDTO());
        mv.addObject("moderationStatus", exercise.getModerationStatus());

        List<Map<String, String>> muscleTargets = exercise.getMuscleGroupTarget().stream()
                .map(target -> Map.of(
                        "muscle", target.getMuscle().name(),
                        "intensity", target.getIntensity().name()
                ))
                .toList();

        mv.addObject("simplifiedMuscleTargets", muscleTargets);
    }

    private static void buildWorkoutView(Workout workout, ModelAndView mv) {
        List<CreateWorkoutExerciseDTO> exerciseDTOs = workout.getWorkoutExercises().stream()
                .map(we -> CreateWorkoutExerciseDTO.builder()
                        .id(we.getId())
                        .exerciseId(we.getExercise().getId())
                        .type(we.getExercise().getType())
                        .number(we.getNumber())
                        .reps(we.getReps())
                        .weight(we.getWeight())
                        .duration(we.getDuration())
                        .build()
                ).toList();

        CreateWorkoutDTO dto = CreateWorkoutDTO.builder()
                .name(workout.getName())
                .type(workout.getType())
                .isPublic(workout.getIsPublic())
                .exercises(exerciseDTOs)
                .build();

        mv.addObject("createWorkoutDTO", dto);
        mv.addObject("createExerciseDTO", new CreateExerciseDTO());
        mv.addObject("moderationStatus", workout.getModerationStatus());

        try {
            mv.addObject("safeWorkoutExercisesJson", objectMapper.writeValueAsString(exerciseDTOs));
        } catch (Exception e) {
            mv.addObject("safeWorkoutExercisesJson", "[]");
        }
    }

    private static void buildPlanView(Plan plan, ModelAndView mv) {
        List<CreatePlanDayDTO> planDayDTOs = plan.getPlanDays().stream()
                .map(planDay -> {
                    List<UUID> workoutIds = planDay.getWorkouts().stream()
                            .map(Workout::getId)
                            .collect(Collectors.toList());

                    return CreatePlanDayDTO.builder()
                            .dayNumber(planDay.getDayNumber())
                            .type(planDay.getType())
                            .workoutIds(workoutIds)
                            .build();
                })
                .collect(Collectors.toList());

        CreatePlanDTO dto = CreatePlanDTO.builder()
                .name(plan.getName())
                .description(plan.getDescription())
                .type(plan.getType())
                .isPublic(plan.getIsPublic())
                .days(planDayDTOs)
                .build();

        mv.addObject("createPlanDTO", dto);
        mv.addObject("createExerciseDTO", new CreateExerciseDTO());
        mv.addObject("createWorkoutDTO", new CreateWorkoutDTO());
        mv.addObject("moderationStatus", plan.getModerationStatus());

        try {
            Map<String, Object> planEditData = new HashMap<>();

            planEditData.put("name", plan.getName());
            planEditData.put("description", plan.getDescription());
            planEditData.put("type", plan.getType());
            planEditData.put("isPublic", plan.getIsPublic());

            List<Map<String, Object>> daysData = new ArrayList<>();

            for (PlanDay planDay : plan.getPlanDays()) {
                Map<String, Object> dayData = new HashMap<>();

                dayData.put("dayNumber", planDay.getDayNumber());
                dayData.put("type", planDay.getType());

                List<Map<String, Object>> workoutsData = new ArrayList<>();

                for (Workout workout : planDay.getWorkouts()) {
                    Map<String, Object> workoutData = new HashMap<>();

                    workoutData.put("id", workout.getId());
                    workoutData.put("name", workout.getName());
                    workoutData.put("type", workout.getType());
                    workoutData.put("exerciseCount", workout.getWorkoutExercises() != null ? workout.getWorkoutExercises().size() : 0);

                    List<Map<String, Object>> exercisesData = new ArrayList<>();

                    if (workout.getWorkoutExercises() != null) {
                        for (WorkoutExercise workoutExercise : workout.getWorkoutExercises()) {
                            Map<String, Object> exerciseData = new HashMap<>();

                            exerciseData.put("id", workoutExercise.getExercise().getId());
                            exerciseData.put("name", workoutExercise.getExercise().getName());
                            exerciseData.put("type", workoutExercise.getExercise().getType());
                            exerciseData.put("imageUrl", workoutExercise.getExercise().getImageUrl());
                            exerciseData.put("reps", workoutExercise.getReps());
                            exerciseData.put("weight", workoutExercise.getWeight());
                            exerciseData.put("duration", workoutExercise.getDuration());

                            List<Map<String, Object>> muscleGroupsData = new ArrayList<>();

                            if (workoutExercise.getExercise().getMuscleGroupTarget() != null) {

                                for (MuscleTarget muscleTarget : workoutExercise.getExercise().getMuscleGroupTarget()) {
                                    Map<String, Object> muscleData = new HashMap<>();

                                    muscleData.put("muscle", muscleTarget.getMuscle().getDisplayName());
                                    muscleData.put("intensity", muscleTarget.getIntensity().name());

                                    muscleGroupsData.add(muscleData);
                                }
                            }
                            exerciseData.put("muscleGroups", muscleGroupsData);
                            exercisesData.add(exerciseData);
                        }
                    }
                    workoutData.put("exercises", exercisesData);
                    workoutsData.add(workoutData);
                }
                dayData.put("workouts", workoutsData);
                daysData.add(dayData);
            }
            planEditData.put("days", daysData);

            String planJson = objectMapper.writeValueAsString(planEditData);
            mv.addObject("planJson", planJson);
        } catch (Exception e) {
            mv.addObject("planJson", "{}");
        }
    }
}

