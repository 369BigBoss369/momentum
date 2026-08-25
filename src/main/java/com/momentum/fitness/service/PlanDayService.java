package com.momentum.fitness.service;

import com.momentum.fitness.model.PlanDay;
import com.momentum.fitness.repository.PlanDayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PlanDayService {
    private final PlanDayRepository planDayRepository;

    @Autowired
    public PlanDayService(PlanDayRepository planDayRepository) {
        this.planDayRepository = planDayRepository;
    }

    public List<PlanDay> getByPlanIdAndDayNumber(UUID planId, int dayNumber) {
        return planDayRepository.findByPlanIdAndDayNumber(planId, dayNumber);
    }
}

