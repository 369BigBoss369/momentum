package com.momentum.fitness.dto;

import com.momentum.fitness.model.enums.PlanDayType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class CreatePlanDayDTO {
    @NotNull(message = "Day number is required")
    @Positive(message = "Day number must be positive")
    private Integer dayNumber;

    @NotNull(message = "Day type is required")
    private PlanDayType type;

    @Builder.Default
    private List<UUID> workoutIds = new ArrayList<>();
}

