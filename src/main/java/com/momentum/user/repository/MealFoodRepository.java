package com.momentum.user.repository;

import com.momentum.user.model.MealFood;
import com.momentum.user.model.enums.MealType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MealFoodRepository extends JpaRepository<MealFood, UUID> {
    List<MealFood> findByMeal_User_IdAndMeal_MealTypeAndMeal_EatenAtBetween(UUID userId, MealType mealType, LocalDateTime start, LocalDateTime end);
    List<MealFood> findByMeal_User_IdOrderByMeal_EatenAtDesc(UUID userId, Pageable pageable);
}

