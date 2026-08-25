package com.momentum.fitness.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratePlanRequest {
    private String duration;
    private String frequency;
    private String goals;
    private String experience;
    private String userId;
}


