package com.momentum.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

import com.momentum.user.model.enums.MealType;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class MealFoodView {
    private UUID id;
    private UUID foodId;
    private String foodName;
    private String foodImagePath;
    private MealType mealType;
    private Integer amount;
    private Integer calories;
    private Double carbohydrates;
    private Double protein;
    private Double fat;
    private Double sugar;
    private Double fiber;
    private Double saturatedFat;
    private Double monoUnsaturated;
    private Double polyUnsaturated;
    private Double transFat;
    private Double sodium;
    private Double potassium;
    private Double calcium;
    private Double cholesterol;
    private Double caffeine;
    private Double alcohol;
    private LocalDateTime eatenAt;
}


