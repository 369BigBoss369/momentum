package com.momentum.fitness.dto;

import com.momentum.fitness.model.enums.PlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class PlanSummaryDTO {
    private UUID id;

    private String name;
    private String description;
    private PlanType type;

    @Builder.Default
    private List<PlanDaySummaryDTO> days = new ArrayList<>();

    private int totalWorkouts;
    private boolean isInLibrary;
}












