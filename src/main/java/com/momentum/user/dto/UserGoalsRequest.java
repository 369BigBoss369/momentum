package com.momentum.user.dto;

import com.momentum.user.model.enums.UserGoal;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class UserGoalsRequest {
    @NotNull(message = "Goal is required")
    private UserGoal goal;

    @DecimalMin(value = "20.0", message = "Target weight must be at least 20 kg")
    @DecimalMax(value = "300.0", message = "Target weight cannot exceed 300 kg")
    private Double targetWeight;

    @DecimalMin(value = "0.1", message = "Pace must be greater than 0")
    @DecimalMax(value = "2.0", message = "Pace cannot exceed 2.0 kg/week")
    private Double pace;
}

