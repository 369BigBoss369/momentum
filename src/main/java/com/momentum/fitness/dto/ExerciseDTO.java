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

public class ExerciseDTO {
    private UUID id;
    private String name;
    private ExerciseType type;
    private List<String> muscleGroups;
    private String imageUrl;
    private String videoUrl;

    public static ExerciseDTO from(Exercise exercise) {
        return ExerciseDTO.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .type(exercise.getType())
                .muscleGroups(exercise.getMuscleGroupTarget() != null ?
                    exercise.getMuscleGroupTarget().stream()
                        .map(mt -> mt.getMuscle().getDisplayName())
                        .distinct()
                        .collect(Collectors.toList()) : null)
                .imageUrl(exercise.getImageUrl())
                .videoUrl(exercise.getVideoUrl())
                .build();
    }
}
























