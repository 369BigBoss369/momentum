package com.momentum.nutrition.dto.seed;

import com.momentum.nutrition.model.enums.ProductType;
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

public class ProductSeedDTO extends BaseFoodSeedDTO {
    private ProductType type;
}

