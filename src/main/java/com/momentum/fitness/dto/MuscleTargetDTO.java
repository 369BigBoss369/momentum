package com.momentum.fitness.dto;

import com.momentum.fitness.model.enums.Intensity;
import com.momentum.fitness.model.enums.MuscleType;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class MuscleTargetDTO {
    private MuscleType muscleType;

    private Intensity intensity;
}

