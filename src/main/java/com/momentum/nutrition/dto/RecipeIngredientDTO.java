package com.momentum.nutrition.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RecipeIngredientDTO {
    @NotNull
    private UUID foodId;

    @NotNull
    private Integer servingSize;

    private String foodName;

    private String imagePath;
}

