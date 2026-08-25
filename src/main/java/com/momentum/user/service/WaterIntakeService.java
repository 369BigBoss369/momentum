package com.momentum.user.service;

import com.momentum.user.dto.WaterIntakeView;
import com.momentum.user.model.User;
import com.momentum.user.model.WaterIntake;
import com.momentum.user.repository.WaterIntakeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class WaterIntakeService {
    private final WaterIntakeRepository waterIntakeRepository;
    private final UserService userService;
    private final NutritionActivityService nutritionActivityService;

    @Autowired
    public WaterIntakeService(WaterIntakeRepository waterIntakeRepository,
                              UserService userService,
                              NutritionActivityService nutritionActivityService) {
        this.waterIntakeRepository = waterIntakeRepository;
        this.userService = userService;
        this.nutritionActivityService = nutritionActivityService;
    }

    @Transactional
    public void logWater(UUID userId, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Water amount must be positive");
        }

        User user = userService.getById(userId);

        WaterIntake intake = WaterIntake.builder()
                .user(user)
                .amount(amount)
                .drankAt(LocalDateTime.now())
                .build();

        waterIntakeRepository.save(intake);

        nutritionActivityService.logWaterLogged(userId, amount);
    }

    @Transactional(readOnly = true)
    public WaterIntakeView getWaterForDate(UUID userId, LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        LocalDateTime dayStart = targetDate.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1).minusSeconds(1);

        Double total = waterIntakeRepository.sumAmountByUserAndDrankAtBetween(userId, dayStart, dayEnd);
        if (total == null) {
            total = 0.0;
        }

        User user = userService.getById(userId);
        Integer goal = user.getDailyWaterGoal();

        return WaterIntakeView.builder()
                .totalAmount(total)
                .goal(goal != null ? goal : 0)
                .build();
    }
}

