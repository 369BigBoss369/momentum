package com.momentum.fitness.dto;

import com.momentum.fitness.model.Exercise;
import com.momentum.fitness.model.enums.ExerciseType;
import lombok.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class ExerciseSearchDTO {
    private UUID id;
    private String name;
    private ExerciseType type;
    private String imagePath;
    private List<String> muscleGroups;

    public static ExerciseSearchDTO from(Exercise exercise) {
        return ExerciseSearchDTO.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .type(exercise.getType())
                .imagePath(exercise.getImageUrl())
                .muscleGroups(exercise.getMuscleGroupTarget().stream()
                        .map(mt -> mt.getMuscle().getDisplayName())
                        .distinct()
                        .collect(Collectors.toList()))
                .build();
    }
}

