package com.momentum.nutrition.dto;

import com.momentum.nutrition.dto.enums.FoodItemType;
import com.momentum.nutrition.model.Food;
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
public class FoodSearchView {

    private UUID id;
    private String name;
    private String imagePath;

    private Integer calories;
    private Double carbohydrates;
    private Double protein;
    private Double fat;

    private Integer ingredientCount;
    private UUID ownerId;
    private Boolean isPublic;

    private FoodItemType itemType;
    private String foodType;

    private Boolean isOwner;
    private Integer sharedUsersCount;
    private String ownerUsername;
    private Boolean isInLibrary;

    public static <T extends Food> FoodSearchView from(T food, UUID currentUserId) {
        return from(food, currentUserId, null);
    }

    public static <T extends Food> FoodSearchView from(T food, UUID currentUserId, String ownerUsername) {
        if (food == null) {
            return null;
        }

        Integer sharedCount = food.getSharedUsers() != null
                ? food.getSharedUsers().size()
                : 0;

        boolean isOwner = currentUserId != null && currentUserId.equals(food.getOwnerId());
        
        boolean isInLibrary = currentUserId != null && food.getSharedUsers() != null
                && food.getSharedUsers().stream().anyMatch(u -> u.getId().equals(currentUserId));

        return FoodSearchView.builder()
                .id(food.getId())
                .name(food.getName())
                .imagePath(food.getImagePath())
                .calories(food.getCalories())
                .carbohydrates(food.getCarbohydrates())
                .protein(food.getProtein())
                .fat(food.getFat())
                .ownerId(food.getOwnerId())
                .isPublic(food.getIsPublic())
                .isOwner(isOwner)
                .sharedUsersCount(sharedCount)
                .ownerUsername(ownerUsername)
                .isInLibrary(isInLibrary)
                .build();
    }
}


