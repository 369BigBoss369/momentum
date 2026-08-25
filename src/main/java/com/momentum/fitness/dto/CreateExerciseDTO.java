package com.momentum.fitness.dto;

import com.momentum.fitness.model.enums.ExerciseType;
import com.momentum.fitness.model.MuscleTarget;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class CreateExerciseDTO {
    @NotBlank(message = "Exercise name is required")
    private String name;

    @NotNull(message = "Exercise type is required")
    private ExerciseType type;

    @Builder.Default
    @Size(min = 1, message = "At least one muscle target is required")
    private List<MuscleTarget> muscleTargets = new ArrayList<>();

    @Size(max = 1024, message = "Image URL must be less than 1024 characters")
    private String imageUrl;

    @Size(max = 1024, message = "Video URL must be less than 1024 characters")
    private String videoUrl;

    @Builder.Default
    private Boolean isPublic = false;
}

