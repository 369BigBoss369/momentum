package com.momentum.nutrition.dto;

import com.momentum.nutrition.model.enums.CompositeFoodType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.URL;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class CreateRecipeDTO implements EditableFoodData {
    @NotBlank
    @Size(min = 3, max = 100)
    private String title;
    @NotNull
    private CompositeFoodType type;
    @URL
    @Size(max = 2048, message = "Image URL must be at most 2048 characters")
    private String imagePath;
    @NotNull
    @Min(1)
    private Integer servingSize;
    @Builder.Default
    private Boolean isPublic = false;

    @Builder.Default
    private List<RecipeIngredientDTO> ingredients = new ArrayList<>();
    @Builder.Default
    private List<CreateStepDTO> steps = new ArrayList<>();

    @Override
    public String getName() {
        return this.title;
    }
}

