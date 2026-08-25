package com.momentum.fitness.dto;

import com.momentum.fitness.model.Workout;
import com.momentum.fitness.model.enums.WorkoutType;
import lombok.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class WorkoutSearchDTO {
    private UUID id;
    private String name;
    private WorkoutType type;
    private boolean isPublic;
    private int exerciseCount;
    private List<WorkoutExerciseDTO> exercises;

    public static WorkoutSearchDTO from(Workout workout) {
        List<WorkoutExerciseDTO> exerciseDTOs = null;
        if (workout.getWorkoutExercises() != null) {
            exerciseDTOs = workout.getWorkoutExercises().stream()
                    .map(WorkoutExerciseDTO::from)
                    .collect(Collectors.toList());
        }
        return WorkoutSearchDTO.builder()
                .id(workout.getId())
                .name(workout.getName())
                .type(workout.getType())
                .isPublic(workout.getIsPublic() != null ? workout.getIsPublic() : false)
                .exerciseCount(workout.getWorkoutExercises() != null ? workout.getWorkoutExercises().size() : 0)
                .exercises(exerciseDTOs)
                .build();
    }
}

