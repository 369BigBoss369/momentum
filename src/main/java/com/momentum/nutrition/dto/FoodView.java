package com.momentum.nutrition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class FoodView {


    private UUID id;
    private String name;
    private String imagePath;


    private Integer calories;
    private Double carbohydrates;
    private Double protein;
    private Double fat;


    private UUID ownerId;
    private Boolean isPublic;
    private String type;


    private Boolean isOwner;
    private Integer sharedUsersCount;
    private String ownerUsername;
}





























