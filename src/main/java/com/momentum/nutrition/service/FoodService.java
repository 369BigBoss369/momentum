package com.momentum.nutrition.service;

import com.momentum.exception.nutrition.FoodNotFoundException;
import com.momentum.nutrition.model.Food;
import com.momentum.nutrition.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FoodService {
    private final FoodRepository foodRepository;

    @Autowired
    public FoodService(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    public Food getById(UUID id) {
        return foodRepository.findById(id).orElseThrow(() -> new FoodNotFoundException(id.toString()));
    }

    public List<Food> getAll() {
        return foodRepository.findAll();
    }

    public List<Food> searchByName(String query, int limit, UUID userId) {
        Pageable pageable = PageRequest.of(0, Math.max(1, Math.min(limit, 50)), Sort.by("name"));

        if (query == null || query.trim().length() < 2) {
            return foodRepository.findAllAccessible(userId, userId, pageable).getContent();
        }

        return foodRepository.searchByName(query.trim(), userId, userId, pageable).getContent();
    }
}

