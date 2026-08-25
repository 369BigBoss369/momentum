package com.momentum.nutrition.dto.seed;

import com.momentum.nutrition.dto.BaseFoodData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter

public abstract class BaseFoodSeedDTO implements BaseFoodData {
    private String name;
    private String imagePath;

    private Integer calories;

    private Double carbohydrates;
    private Double sugar;
    private Double fiber;
    private Double glycemicIndex;
    private Double glycemicLoad;

    private Double protein;

    private Double fat;
    private Double saturatedFat;
    private Double monoUnsaturated;
    private Double polyUnsaturated;
    private Double transFat;

    private Double cholesterol;
    private Double caffeine;
    private Double alcohol;
    private Double sodium;
    private Double potassium;
    private Double calcium;
    private Double iron;

    public Integer getServingSize() {
        return null;
    }

    public void setServingSize(Integer servingSize) {
        return;
    }
}

