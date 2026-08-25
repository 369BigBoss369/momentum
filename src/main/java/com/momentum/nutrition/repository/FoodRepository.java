package com.momentum.nutrition.repository;

import com.momentum.nutrition.model.Food;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface FoodRepository extends JpaRepository<Food, UUID> {
    @Query("""
        SELECT f FROM Food f
        WHERE (f.source = 'DEFAULT' OR
               (f.source = 'CUSTOM' AND f.ownerId = :userId) OR
               (f.source = 'SHARED' AND EXISTS (SELECT 1 FROM f.sharedUsers u WHERE u.id = :sharedUserId)))
        ORDER BY f.name ASC
    """)
    Page<Food> findAllAccessible(UUID userId, UUID sharedUserId, Pageable pageable);

    @Query("""
        SELECT f FROM Food f
        WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :query, '%'))
        AND (f.source = 'DEFAULT' OR
             (f.source = 'CUSTOM' AND f.ownerId = :userId) OR
             (f.source = 'SHARED' AND EXISTS (SELECT 1 FROM f.sharedUsers u WHERE u.id = :sharedUserId)))
        ORDER BY
            CASE
                WHEN LOWER(f.name) = LOWER(:query) THEN 0
                WHEN LOWER(f.name) LIKE LOWER(CONCAT(:query, ' %')) THEN 1
                WHEN LOWER(f.name) LIKE LOWER(CONCAT(:query, '%')) THEN 2
                ELSE 3
            END,
            LENGTH(f.name) ASC,
            LOCATE(LOWER(:query), LOWER(f.name)) ASC
    """)
    Page<Food> searchByName(String query, UUID userId, UUID sharedUserId, Pageable pageable);
}

