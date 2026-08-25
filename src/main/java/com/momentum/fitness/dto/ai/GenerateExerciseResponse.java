package com.momentum.fitness.dto.ai;

import com.momentum.fitness.model.enums.ExerciseType;
import com.momentum.fitness.model.MuscleTarget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateExerciseResponse {
    private String name;
    private ExerciseType type;
    private List<MuscleTarget> muscleTargets;
    private String imageUrl;
    private String videoUrl;
    private boolean success;
    private String errorMessage;
}

