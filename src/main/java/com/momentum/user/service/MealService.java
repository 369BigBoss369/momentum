package com.momentum.user.service;

import com.momentum.exception.nutrition.FoodNotFoundException;
import com.momentum.exception.UnauthorizedResourceAccessException;
import com.momentum.nutrition.model.Food;
import com.momentum.nutrition.service.FoodService;
import com.momentum.user.dto.DailyNutritionView;
import com.momentum.user.dto.MealFoodDTO;
import com.momentum.user.dto.MealFoodView;
import com.momentum.user.model.Meal;
import com.momentum.user.model.MealFood;
import com.momentum.user.model.User;
import com.momentum.user.model.enums.GenderType;
import com.momentum.user.model.enums.MealType;
import com.momentum.user.repository.MealFoodRepository;
import com.momentum.user.repository.MealRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MealService {
    private final MealRepository mealRepository;
    private final MealFoodRepository mealFoodRepository;
    private final UserService userService;
    private final FoodService foodService;
    private final NutritionActivityService nutritionActivityService;

    @Autowired
    public MealService(MealRepository mealRepository,
                       MealFoodRepository mealFoodRepository,
                       UserService userService,
                       FoodService foodService,
                       NutritionActivityService nutritionActivityService) {
        this.mealRepository = mealRepository;
        this.mealFoodRepository = mealFoodRepository;
        this.userService = userService;
        this.foodService = foodService;
        this.nutritionActivityService = nutritionActivityService;
    }

    @Transactional
    public void addFoodToMeal(String mealType, MealFoodDTO mealFoodDto, User user, String date) {
        MealType type = MealType.valueOf(mealType.toUpperCase().replace("-", "_"));
        LocalDate targetDate = resolveAndValidateTargetDate(date);
        Meal meal = getOrCreateMeal(type, user.getId(), targetDate);

        Food food = foodService.getById(mealFoodDto.getFoodId());

        MealFood mealFood = MealFood.builder()
                .meal(meal)
                .food(food)
                .servingSize(mealFoodDto.getAmount())
                .build();

        mealFoodRepository.save(mealFood);

        nutritionActivityService.logMealAdded(user.getId(), type, food.getName(), mealFoodDto.getAmount());
    }

    @Transactional
    public void removeMealFood(UUID mealFoodId, UUID userId) {
        MealFood mealFood = mealFoodRepository.findById(mealFoodId)
                .orElseThrow(() -> new FoodNotFoundException("Meal food does not exist"));

        UUID ownerId = mealFood.getMeal().getUser().getId();
        if (!ownerId.equals(userId)) {
            throw new UnauthorizedResourceAccessException("You do not have permission to modify this meal");
        }

        LocalDate mealDate = mealFood.getMeal().getEatenAt().toLocalDate();
        validateNotPast(mealDate);

        mealFoodRepository.delete(mealFood);

        nutritionActivityService.logMealRemoved(userId,
                mealFood.getMeal().getMealType(),
                mealFood.getFood().getName(),
                mealFood.getServingSize());
    }

    @Transactional
    public Meal getOrCreateMeal(MealType mealType, UUID userId, LocalDate date) {
        User user = userService.getById(userId);

        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        LocalDateTime dayStart = targetDate.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1).minusSeconds(1);

        List<Meal> existingMeals = mealRepository.findByUser_IdAndMealTypeAndEatenAtBetween(
                userId, mealType, dayStart, dayEnd);

        if (!existingMeals.isEmpty()) {
            return existingMeals.get(0);
        }

        return mealRepository.save(
                Meal.builder()
                        .mealType(mealType)
                        .eatenAt(dayStart)
                        .user(user)
                        .build()
        );
    }

    private LocalDate resolveAndValidateTargetDate(String date) {
        LocalDate targetDate;
        if (date == null || date.isBlank()) {
            targetDate = LocalDate.now();
        } else {
            try {
                targetDate = LocalDate.parse(date);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format. Expected yyyy-MM-dd");
            }
        }
        validateNotPast(targetDate);
        return targetDate;
    }

    private void validateNotPast(LocalDate targetDate) {
        LocalDate today = LocalDate.now();
        if (targetDate.isBefore(today)) {
            throw new UnauthorizedResourceAccessException("Cannot modify meals for past dates");
        }
    }

    @Transactional(readOnly = true)
    public List<MealFoodView> getMealsForDate(UUID userId, LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1).minusSeconds(1);

        List<MealFoodView> items = new java.util.ArrayList<>();

        for (MealType mealType : MealType.values()) {
            List<MealFood> mealFoods = mealFoodRepository.findByMeal_User_IdAndMeal_MealTypeAndMeal_EatenAtBetween(userId, mealType, dayStart, dayEnd);

            List<MealFoodView> meals = mealFoods.stream()
                    .map(this::mealFoodToMealFoodDTO)
                    .toList();

            items.addAll(meals);
        }

        return items;
    }

    @Transactional(readOnly = true)
    public Map<String, List<MealFoodView>> getMealsInRange(UUID userId, LocalDate start, LocalDate end) {
        Map<String, List<MealFoodView>> result = new HashMap<>();

        LocalDate current = start;
        while (!current.isAfter(end)) {
            List<MealFoodView> dayMeals = getMealsForDate(userId, current);
            String dateKey = current.toString();
            result.put(dateKey, dayMeals);
            current = current.plusDays(1);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<MealFoodView> getRecentMeals(UUID userId, int limit) {
        int validatedLimit = (limit <= 0 || limit > 50) ? 10 : limit;
        PageRequest pageRequest = PageRequest.of(0, validatedLimit);

        List<MealFood> foods = mealFoodRepository.findByMeal_User_IdOrderByMeal_EatenAtDesc(userId, pageRequest);

        return foods.stream()
                .map(this::mealFoodToMealFoodDTO)
                .toList();
    }

    private MealFoodView mealFoodToMealFoodDTO(MealFood mealFood) {
        Food food = mealFood.getFood();
        Integer amount = mealFood.getServingSize();
        double multiplier = amount / 100.0;

        return MealFoodView.builder()
                .id(mealFood.getId())
                .foodId(food.getId())
                .foodName(food.getName())
                .foodImagePath(food.getImagePath())
                .mealType(mealFood.getMeal().getMealType())
                .amount(amount)
                .calories((int) Math.round((food.getCalories() * multiplier)))
                .carbohydrates(food.getCarbohydrates() * multiplier)
                .protein(food.getProtein() * multiplier)
                .fat(food.getFat() * multiplier)
                .sugar(food.getSugar() * multiplier)
                .fiber(food.getFiber() * multiplier)
                .saturatedFat(food.getSaturatedFat() * multiplier)
                .monoUnsaturated((food.getMonoUnsaturated() != null ? food.getMonoUnsaturated() : 0) * multiplier)
                .polyUnsaturated((food.getPolyUnsaturated() != null ? food.getPolyUnsaturated() : 0) * multiplier)
                .transFat((food.getTransFat() != null ? food.getTransFat() : 0) * multiplier)
                .sodium(food.getSodium() * multiplier)
                .potassium((food.getPotassium() != null ? food.getPotassium() : 0) * multiplier)
                .calcium((food.getCalcium() != null ? food.getCalcium() : 0) * multiplier)
                .cholesterol((food.getCholesterol() != null ? food.getCholesterol() : 0) * multiplier)
                .caffeine((food.getCaffeine() != null ? food.getCaffeine() : 0) * multiplier)
                .alcohol((food.getAlcohol() != null ? food.getAlcohol() : 0) * multiplier)
                .eatenAt(mealFood.getMeal().getEatenAt())
                .build();
    }

    @Transactional(readOnly = true)
    public DailyNutritionView buildDailyNutritionView(User user) {
        List<MealFoodView> meals = getMealsForDate(user.getId(), LocalDate.now());

        int caloriesConsumed = meals.stream()
                .mapToInt(MealFoodView::getCalories)
                .sum();
        int carbsConsumed = (int) Math.round(meals.stream()
                .mapToDouble(MealFoodView::getCarbohydrates)
                .sum());
        int proteinConsumed = (int) Math.round(meals.stream()
                .mapToDouble(MealFoodView::getProtein)
                .sum());
        int fatConsumed = (int) Math.round(meals.stream()
                .mapToDouble(MealFoodView::getFat)
                .sum());

        int sugarGoal = roundPercentage(user.getMaxCarbohydrates(), 0.1);
        int saturatedFatGoal = roundPercentage(user.getMaxFat(), 0.1);
        int monoUnsaturatedGoal = roundPercentage(user.getMaxFat(), 0.4);
        int polyUnsaturatedGoal = roundPercentage(user.getMaxFat(), 0.3);
        int transFatGoal = 0;
        int ironGoal = user.getGender() == GenderType.MALE ? 8 : 18;
        int fiberGoal = 25;
        int glycemicIndex = 58;
        int glycemicLoad = 12;
        int sodiumGoal = 2300;
        int potassiumGoal = 3500;
        int calciumGoal = 1000;
        int cholesterolGoal = 300;
        int caffeineGoal = 400;
        int alcoholGoal = 0;

        int caloriesGoal = safeInt(user.getMaxCalories());
        int carbsGoal = safeInt(user.getMaxCarbohydrates());
        int proteinGoal = safeInt(user.getMaxProtein());
        int fatGoal = safeInt(user.getMaxFat());

        double sugarSum = meals.stream()
                .mapToDouble(meal -> meal.getSugar() != null ? meal.getSugar() : 0)
                .sum();
        double fiberSum = meals.stream()
                .mapToDouble(meal -> meal.getFiber() != null ? meal.getFiber() : 0)
                .sum();
        double saturatedFatSum = meals.stream()
                .mapToDouble(meal -> meal.getSaturatedFat() != null ? meal.getSaturatedFat() : 0)
                .sum();
        double monoUnsaturatedSum = meals.stream()
                .mapToDouble(meal -> meal.getMonoUnsaturated() != null ? meal.getMonoUnsaturated() : 0)
                .sum();
        double polyUnsaturatedSum = meals.stream()
                .mapToDouble(meal -> meal.getPolyUnsaturated() != null ? meal.getPolyUnsaturated() : 0)
                .sum();
        double transFatSum = meals.stream()
                .mapToDouble(meal -> meal.getTransFat() != null ? meal.getTransFat() : 0)
                .sum();
        double sodiumSum = meals.stream()
                .mapToDouble(meal -> meal.getSodium() != null ? meal.getSodium() : 0)
                .sum();
        double potassiumSum = meals.stream()
                .mapToDouble(meal -> meal.getPotassium() != null ? meal.getPotassium() : 0)
                .sum();
        double calciumSum = meals.stream()
                .mapToDouble(meal -> meal.getCalcium() != null ? meal.getCalcium() : 0)
                .sum();
        double cholesterolSum = meals.stream()
                .mapToDouble(meal -> meal.getCholesterol() != null ? meal.getCholesterol() : 0)
                .sum();
        double caffeineSum = meals.stream()
                .mapToDouble(meal -> meal.getCaffeine() != null ? meal.getCaffeine() : 0)
                .sum();
        double alcoholSum = meals.stream()
                .mapToDouble(meal -> meal.getAlcohol() != null ? meal.getAlcohol() : 0)
                .sum();

        return DailyNutritionView.builder()
                .caloriesGoal(caloriesGoal)
                .carbsGoal(carbsGoal)
                .proteinGoal(proteinGoal)
                .fatGoal(fatGoal)
                .sugarGoal(sugarGoal)
                .fiberGoal(fiberGoal)
                .saturatedFatGoal(saturatedFatGoal)
                .monoUnsaturatedGoal(monoUnsaturatedGoal)
                .polyUnsaturatedGoal(polyUnsaturatedGoal)
                .transFatGoal(transFatGoal)
                .ironGoal(ironGoal)
                .glycemicIndex(glycemicIndex)
                .glycemicLoad(glycemicLoad)
                .sodiumGoal(sodiumGoal)
                .potassiumGoal(potassiumGoal)
                .calciumGoal(calciumGoal)
                .cholesterolGoal(cholesterolGoal)
                .caffeineGoal(caffeineGoal)
                .alcoholGoal(alcoholGoal)
                .caloriesConsumed(caloriesConsumed)
                .carbsConsumed(carbsConsumed)
                .proteinConsumed(proteinConsumed)
                .fatConsumed(fatConsumed)
                .sugarConsumed(roundDouble(sugarSum))
                .fiberConsumed(roundDouble(fiberSum))
                .saturatedFatConsumed(roundDouble(saturatedFatSum))
                .monoUnsaturatedConsumed(roundDouble(monoUnsaturatedSum))
                .polyUnsaturatedConsumed(roundDouble(polyUnsaturatedSum))
                .transFatConsumed(roundDouble(transFatSum))
                .sodiumConsumed(roundDouble(sodiumSum))
                .potassiumConsumed(roundDouble(potassiumSum))
                .calciumConsumed(roundDouble(calciumSum))
                .cholesterolConsumed(roundDouble(cholesterolSum))
                .caffeineConsumed(roundDouble(caffeineSum))
                .alcoholConsumed(roundDouble(alcoholSum))
                .build();
    }

    public DailyNutritionView buildEmptyDailyNutritionView() {
        return DailyNutritionView.builder()
                .caloriesGoal(0)
                .carbsGoal(0)
                .proteinGoal(0)
                .fatGoal(0)
                .sugarGoal(0)
                .fiberGoal(25)
                .sugarConsumed(0)
                .fiberConsumed(0)
                .saturatedFatConsumed(0)
                .monoUnsaturatedConsumed(0)
                .polyUnsaturatedConsumed(0)
                .transFatConsumed(0)
                .sodiumConsumed(0)
                .potassiumConsumed(0)
                .calciumConsumed(0)
                .cholesterolConsumed(0)
                .caffeineConsumed(0)
                .alcoholConsumed(0)
                .saturatedFatGoal(0)
                .monoUnsaturatedGoal(0)
                .polyUnsaturatedGoal(0)
                .transFatGoal(0)
                .ironGoal(18)
                .glycemicIndex(58)
                .glycemicLoad(12)
                .sodiumGoal(2300)
                .potassiumGoal(3500)
                .calciumGoal(1000)
                .cholesterolGoal(300)
                .caffeineGoal(400)
                .alcoholGoal(0)
                .caloriesConsumed(0)
                .carbsConsumed(0)
                .proteinConsumed(0)
                .fatConsumed(0)
                .build();
    }

    private int roundPercentage(Integer value, double ratio) {
        if (value == null) {
            return 0;
        }
        return (int) Math.round(value * ratio);
    }

    private int safeInt(Integer value) {
        return value != null ? value : 0;
    }

    private int roundDouble(double value) {
        return (int) Math.round(value);
    }
}

