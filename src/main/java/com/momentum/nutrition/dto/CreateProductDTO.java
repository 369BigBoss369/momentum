package com.momentum.nutrition.dto;

import com.momentum.nutrition.model.enums.ProductType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.URL;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class CreateProductDTO implements BaseFoodData, EditableFoodData {
    @NotBlank
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;
    @NotNull
    private ProductType type;
    @URL
    @Size(max = 2048, message = "Image URL must be at most 2048 characters")
    private String imagePath;
    @NotNull
    @Min(value = 1, message = "Serving size must be at least 1 gram")
    private Integer servingSize;
    @Builder.Default
    private Boolean isPublic = false;

    @NotNull
    private Integer calories;

    @NotNull
    private Double carbohydrates;
    @NotNull
    private Double sugar;
    @NotNull
    private Double fiber;
    private Double glycemicIndex;
    private Double glycemicLoad;

    @NotNull
    private Double protein;

    @NotNull
    private Double fat;
    @NotNull
    private Double saturatedFat;
    private Double monoUnsaturated;
    private Double polyUnsaturated;
    private Double transFat;

    private Double cholesterol;
    private Double caffeine;
    private Double alcohol;
    @NotNull
    private Double sodium;
    private Double potassium;
    private Double calcium;
    private Double iron;
}

