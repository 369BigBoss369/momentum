package com.momentum.nutrition.service;

import com.momentum.nutrition.dto.FoodSearchDTO;
import com.momentum.nutrition.dto.FoodSearchView;
import com.momentum.nutrition.dto.enums.FoodItemType;
import com.momentum.nutrition.dto.enums.OwnershipType;
import com.momentum.nutrition.model.CompositeFood;
import com.momentum.nutrition.model.Product;
import com.momentum.nutrition.model.Recipe;
import com.momentum.nutrition.model.enums.CompositeFoodType;
import com.momentum.nutrition.model.enums.ProductType;
import com.momentum.util.NutritionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FoodSearchService {
    private final ProductService productService;
    private final CompositeFoodService compositeFoodService;
    private final RecipeService recipeService;

    @Autowired
    public FoodSearchService(ProductService productService, CompositeFoodService compositeFoodService, RecipeService recipeService) {
        this.productService = productService;
        this.compositeFoodService = compositeFoodService;
        this.recipeService = recipeService;
    }

    public List<FoodSearchDTO> getFoodsByType(String type, String foodType, UUID userId) {
        if ("PRODUCT".equals(type)) {
            List<Product> products = productService.getByType(ProductType.valueOf(foodType), userId, userId);
            return NutritionMapper.mapToFoodSearchDTO(products);
        }

        if ("COMPOSITE".equals(type)) {
            CompositeFoodType compositeType = CompositeFoodType.valueOf(foodType);
            List<CompositeFood> compositeFoods = compositeFoodService.getByType(compositeType, userId, userId);
            List<Recipe> recipes = recipeService.getByType(compositeType, userId, userId);
            
            List<FoodSearchDTO> result = new java.util.ArrayList<>();
            result.addAll(NutritionMapper.mapToFoodSearchDTO(compositeFoods));
            result.addAll(NutritionMapper.mapToFoodSearchDTO(recipes));
            
            return result;
        }

        throw new IllegalArgumentException("Invalid food type: " + type);
    }
    
    public List<FoodSearchDTO> searchFoodsByType(String query, String type, String foodType, UUID userId) {
        List<FoodSearchDTO> allFoods = getFoodsByType(type, foodType, userId);
        
        if (query == null || query.trim().isEmpty()) {
            return allFoods;
        }

        return allFoods.stream()
                .filter(food -> food.getName().toLowerCase().contains(query.toLowerCase().trim()))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<FoodSearchView> search(FoodItemType itemType, String name, List<UUID> ingredients, OwnershipType ownership, String foodType, UUID userId, Boolean inLibrary) {
        return switch (itemType) {
            case PRODUCT -> productService.search(name, foodType, userId, inLibrary, ownership);
            case COMPOSITE -> compositeFoodService.search(name, foodType, userId, inLibrary, ownership);
            case RECIPE -> recipeService.search(name, ingredients, ownership, userId, inLibrary);
        };
    }
}

