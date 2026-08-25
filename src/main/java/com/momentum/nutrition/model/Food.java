package com.momentum.nutrition.model;

import com.momentum.core.model.ShareableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "foods")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Food extends ShareableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;
    @Column(length = 2048)
    private String imagePath;

    @Column(nullable = true)
    private Integer servingSize;
    @Column(nullable = false)
    private Integer calories;

    @Column(nullable = false)
    private Double carbohydrates;
    @Column(nullable = false)
    private Double sugar;
    @Column(nullable = false)
    private Double fiber;
    private Double glycemicIndex;
    private Double glycemicLoad;

    @Column(nullable = false)
    private Double protein;

    @Column(nullable = false)
    private Double fat;
    @Column(nullable = false)
    private Double saturatedFat;
    private Double monoUnsaturated;
    private Double polyUnsaturated;
    private Double transFat;

    private Double cholesterol;
    private Double caffeine;
    private Double alcohol;
    @Column(nullable = false)
    private Double sodium;
    private Double potassium;
    private Double calcium;
    private Double iron;
}

