package com.momentum.fitness.dto;

import com.momentum.fitness.model.enums.ExerciseType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CreateWorkoutExerciseDTO {
    private UUID id;

    @NotNull(message = "Exercise ID is required")
    private UUID exerciseId;

    @NotNull
    private ExerciseType type;
    @NotNull
    private Integer number;

    private Integer reps;
    private Double weight;
    private Integer duration;
}

