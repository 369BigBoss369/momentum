package com.momentum.nutrition.dto.seed;

import com.momentum.nutrition.model.enums.CompositeFoodType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter

public class CompositeFoodSeedDTO extends BaseFoodSeedDTO {
    private CompositeFoodType type;
}

