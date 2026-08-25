package com.momentum.fitness.dto;

import com.momentum.fitness.model.enums.WorkoutType;
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

public class CreateWorkoutDTO {
    @NotBlank(message = "Workout name is required")
    private String name;

    @NotNull(message = "Workout type is required")
    private WorkoutType type;

    @Builder.Default
    @Size(min = 1, message = "At least one exercise must be selected")
    private List<CreateWorkoutExerciseDTO> exercises = new ArrayList<>();

    @Builder.Default
    private Boolean isPublic = false;
}

