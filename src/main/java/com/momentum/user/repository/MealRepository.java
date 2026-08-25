package com.momentum.user.repository;

import com.momentum.user.model.Meal;
import com.momentum.user.model.enums.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MealRepository extends JpaRepository<Meal, UUID> {
    List<Meal> findByUser_IdAndMealTypeAndEatenAtBetween(UUID userId, MealType mealType, LocalDateTime before, LocalDateTime after);
}

