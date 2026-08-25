package com.momentum.fitness.model;

import com.momentum.fitness.model.enums.Intensity;
import com.momentum.fitness.model.enums.MuscleType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

@Embeddable
public class MuscleTarget {
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MuscleType muscle;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Intensity intensity;
}

