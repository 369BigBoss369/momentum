package com.momentum.nutrition.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momentum.nutrition.dto.seed.CompositeFoodSeedDTO;
import com.momentum.nutrition.dto.seed.FoodSeedData;
import com.momentum.nutrition.dto.seed.ProductSeedDTO;
import com.momentum.nutrition.service.CompositeFoodService;
import com.momentum.nutrition.service.ProductService;
import com.momentum.util.ImagePathResolver;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class FoodSeeder implements ApplicationRunner {
    private final ProductService productService;
    private final CompositeFoodService compositeFoodService;

    public FoodSeeder(ProductService productService, CompositeFoodService compositeFoodService) {
        this.productService = productService;
        this.compositeFoodService = compositeFoodService;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (productService.getCount() > 0 && compositeFoodService.getCount() > 0) {
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = FoodSeeder.class.getClassLoader().getResourceAsStream("seed/food-data.json");
        FoodSeedData seedData = mapper.readValue(inputStream, FoodSeedData.class);

        seedProducts(seedData.getProducts());
        seedCompositeFoods(seedData.getCompositeFoods());
    }

    private void seedProducts(List<ProductSeedDTO> products) {
        for (ProductSeedDTO product : products) {
            product.setImagePath(ImagePathResolver.resolveProductImage(product.getType()));
            productService.seedProduct(product);
        }
    }

    private void seedCompositeFoods(List<CompositeFoodSeedDTO> compositeFoods) {
        for (CompositeFoodSeedDTO compositeFood : compositeFoods) {
            compositeFood.setImagePath(ImagePathResolver.resolveCompositeFoodImage(compositeFood.getType()));
            compositeFoodService.seedCompositeFood(compositeFood);
        }
    }
}

