package com.momentum.user.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class MealFoodDTO {
    @NotNull
    private UUID foodId;

    @NotNull
    @Min(value = 1, message = "The selected food amount must be at least 1 gram")
    private Integer amount;
}

