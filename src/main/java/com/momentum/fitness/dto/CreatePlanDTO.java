package com.momentum.fitness.dto;

import com.momentum.fitness.model.enums.PlanType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class CreatePlanDTO {
    @NotBlank(message = "Plan name is required")
    private String name;

    private String description;

    @NotNull(message = "Plan type is required")
    private PlanType type;

    @Builder.Default
    private Boolean isPublic = false;

    @Builder.Default
    @Valid
    @Size(min = 1, message = "Plan must have at least one day")
    private List<CreatePlanDayDTO> days = new ArrayList<>();
}

