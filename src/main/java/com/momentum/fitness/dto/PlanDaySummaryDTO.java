package com.momentum.fitness.dto;

import com.momentum.fitness.model.enums.PlanDayType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class PlanDaySummaryDTO {
    private Integer dayNumber;
    private PlanDayType type;

    @Builder.Default
    private List<String> workoutNames = new ArrayList<>();
}



















