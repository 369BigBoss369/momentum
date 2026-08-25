package com.momentum.user.repository;

import com.momentum.user.model.NutritionActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NutritionActivityRepository extends JpaRepository<NutritionActivity, UUID> {
    Page<NutritionActivity> findByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}



