package com.momentum.nutrition.dto;

import com.momentum.nutrition.model.Food;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class FoodSearchDTO {
    private UUID id;
    private String name;
    private String imagePath;
    private Integer calories;
    private Double carbohydrates;
    private Double protein;
    private Double fat;

    public static FoodSearchDTO from(Food food) {
        return FoodSearchDTO.builder()
                .id(food.getId())
                .name(food.getName())
                .imagePath(food.getImagePath())
                .calories(food.getCalories())
                .carbohydrates(food.getCarbohydrates())
                .protein(food.getProtein())
                .fat(food.getFat())
                .build();
    }
}

