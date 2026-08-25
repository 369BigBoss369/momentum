package com.momentum.fitness.service;

import com.momentum.fitness.model.Plan;
import com.momentum.user.model.User;
import com.momentum.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Slf4j
public class PlanStartService {

    private final PlanService planService;
    private final UserService userService;

    @Autowired
    public PlanStartService(PlanService planService, UserService userService) {
        this.planService = planService;
        this.userService = userService;
    }

    
    @Transactional
    public void startPlanForUser(UUID planId, UUID userId) {
        log.info("Starting plan {} for user {}", planId, userId);

        Plan plan = planService.getAccessibleById(planId);
        User user = userService.getById(userId);


        user.setCurrentPlan(plan);
        LocalDate startDate = LocalDate.now();
        user.setCurrentPlanStartDate(startDate);

        userService.save(user);

        log.info("Plan {} started successfully for user {} with start date {}", plan.getName(), userId, startDate);
    }
}

