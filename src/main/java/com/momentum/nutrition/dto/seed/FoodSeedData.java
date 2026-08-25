package com.momentum.nutrition.dto.seed;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class FoodSeedData {
    private List<ProductSeedDTO> products;
    private List<CompositeFoodSeedDTO> compositeFoods;
}

