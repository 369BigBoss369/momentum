package com.momentum.nutrition.web.rest;

import com.momentum.nutrition.dto.FoodSearchDTO;
import com.momentum.nutrition.dto.FoodSearchView;
import com.momentum.nutrition.dto.enums.FoodItemType;
import com.momentum.nutrition.dto.enums.OwnershipType;
import com.momentum.nutrition.service.*;
import com.momentum.user.model.User;
import com.momentum.user.service.UserService;
import com.momentum.user.dto.MealFoodDTO;
import com.momentum.user.dto.MealFoodView;
import com.momentum.user.dto.WaterIntakeRequest;
import com.momentum.user.dto.WaterIntakeView;
import com.momentum.user.dto.NutritionActivityPageResponse;
import com.momentum.user.service.MealService;
import com.momentum.user.service.WaterIntakeService;
import com.momentum.user.service.NutritionActivityService;
import com.momentum.util.NutritionMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/nutrition")
public class NutritionRestController {
    private final FoodService foodService;
    private final MealService mealService;
    private final WaterIntakeService waterIntakeService;
    private final NutritionActivityService nutritionActivityService;
    private final FoodSearchService foodSearchService;
    private final ProductService productService;
    private final CompositeFoodService compositeFoodService;
    private final RecipeService recipeService;
    private final UserService userService;

    @Autowired
    public NutritionRestController(FoodService foodService, MealService mealService, FoodSearchService foodSearchService, ProductService productService, CompositeFoodService compositeFoodService, RecipeService recipeService, WaterIntakeService waterIntakeService, NutritionActivityService nutritionActivityService, UserService userService) {
        this.foodService = foodService;
        this.mealService = mealService;
        this.foodSearchService = foodSearchService;
        this.productService = productService;
        this.compositeFoodService = compositeFoodService;
        this.recipeService = recipeService;
        this.waterIntakeService = waterIntakeService;
        this.nutritionActivityService = nutritionActivityService;
        this.userService = userService;
    }

    @GetMapping(value = "/foods", params = "query")
    public ResponseEntity<List<FoodSearchDTO>> searchFoods(@RequestParam(required = false) String query, @RequestParam(defaultValue = "50") int limit, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        return ResponseEntity.ok(NutritionMapper.mapToFoodSearchDTO(foodService.searchByName(query, limit, user.getId())));
    }

    @GetMapping(value = "/foods", params = {"type", "foodType"})
    public ResponseEntity<List<FoodSearchDTO>> getFoodsByType(@RequestParam(required = false) String query, @RequestParam String type, @RequestParam String foodType, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        return ResponseEntity.ok(foodSearchService.searchFoodsByType(query, type, foodType, user.getId()));
    }

    @GetMapping("/meals")
    public ResponseEntity<List<MealFoodView>> getTodayMeals(@AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        List<MealFoodView> mealsForDate = mealService.getMealsForDate(user.getId(), LocalDate.now());
        return ResponseEntity.ok(mealsForDate);
    }

    @GetMapping(value = "/meals", params = "date")
    public ResponseEntity<List<MealFoodView>> getMealsForDate(@RequestParam String date, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        LocalDate parsedDate = LocalDate.parse(date);
        List<MealFoodView> mealsForDate = mealService.getMealsForDate(user.getId(), parsedDate);
        return ResponseEntity.ok(mealsForDate);
    }

    @GetMapping(value = "/meals", params = {"start", "end"})
    public ResponseEntity<Map<String, List<MealFoodView>>> getMealsInRange(@RequestParam String start, @RequestParam String end, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        return ResponseEntity.ok(mealService.getMealsInRange(user.getId(), startDate, endDate));
    }

    @PostMapping("/meals/food")
    public ResponseEntity<?> addFoodToMeal(@RequestParam String mealType, @RequestParam(required = false) String date, @Valid @RequestBody MealFoodDTO mealFoodDTO, @AuthenticationPrincipal Object principal, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Validation failed: " + bindingResult.getFieldErrors());
        }

        User user = userService.getCurrentUser(principal);
        mealService.addFoodToMeal(mealType, mealFoodDTO, user, date);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/meals/food/{mealFoodId}")
    public ResponseEntity<?> removeFoodFromMeal(@PathVariable UUID mealFoodId, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        mealService.removeMealFood(mealFoodId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/meals/recent")
    public ResponseEntity<List<MealFoodView>> getRecentMeals(@RequestParam(name = "limit", defaultValue = "5") int limit, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        return ResponseEntity.ok(mealService.getRecentMeals(user.getId(), limit));
    }

    @GetMapping("/water")
    public ResponseEntity<WaterIntakeView> getWater(@AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        return ResponseEntity.ok(waterIntakeService.getWaterForDate(user.getId(), LocalDate.now()));
    }

    @PostMapping("/water")
    public ResponseEntity<?> logWater(@Valid @RequestBody WaterIntakeRequest request, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        waterIntakeService.logWater(user.getId(), request.getAmount());
        return ResponseEntity.ok(waterIntakeService.getWaterForDate(user.getId(), LocalDate.now()));
    }

    @GetMapping("/activity")
    public ResponseEntity<NutritionActivityPageResponse> getActivityPage(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        return ResponseEntity.ok(nutritionActivityService.getActivitiesPage(user.getId(), page, size));
    }

    @GetMapping(value = "/foods", params = "itemType")
    public ResponseEntity<List<FoodSearchView>> searchFoods(@RequestParam String itemType, @RequestParam(required = false) String name, @RequestParam(required = false) List<UUID> ingredients, @RequestParam(required = false, defaultValue = "ALL") String ownership, @RequestParam(required = false) String foodType, @RequestParam(required = false) Boolean inLibrary, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        OwnershipType ownershipType = OwnershipType.valueOf(ownership.toUpperCase());
        FoodItemType typeEnum = FoodItemType.valueOf(itemType.toUpperCase());

        List<FoodSearchView> foodSearchViews = foodSearchService.search(typeEnum, name, ingredients, ownershipType, foodType, user.getId(), inLibrary);

        return ResponseEntity.ok(foodSearchViews);
    }

    @PostMapping("/products/{productId}/library")
    public ResponseEntity<?> addProductToLibrary(@PathVariable UUID productId, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        productService.addToLibrary(productId, user.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/products/{productId}/library")
    public ResponseEntity<?> removeProductFromLibrary(@PathVariable UUID productId, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        productService.removeFromLibrary(productId, user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/composites/{compositeId}/library")
    public ResponseEntity<?> addCompositeFoodToLibrary(@PathVariable UUID compositeId, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        compositeFoodService.addToLibrary(compositeId, user.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/composites/{compositeId}/library")
    public ResponseEntity<?> removeCompositeFoodFromLibrary(@PathVariable UUID compositeId, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        compositeFoodService.removeFromLibrary(compositeId, user.getId());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/recipes/{recipeId}/library")
    public ResponseEntity<?> addRecipeToLibrary(@PathVariable UUID recipeId, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        recipeService.addToLibrary(recipeId, user.getId());
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/recipes/{recipeId}/library")
    public ResponseEntity<?> removeRecipeFromLibrary(@PathVariable UUID recipeId, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        recipeService.removeFromLibrary(recipeId, user.getId());
        return ResponseEntity.ok().build();
    }
}

