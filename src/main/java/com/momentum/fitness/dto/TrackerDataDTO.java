package com.momentum.fitness.dto;

import com.momentum.fitness.model.Plan;
import com.momentum.fitness.model.PlanDay;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class TrackerDataDTO {
    private Plan activePlan;
    private int planDaysCount;
    private int currentDay;
    private PlanDay currentPlanDay;
}






