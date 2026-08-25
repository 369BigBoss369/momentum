package com.momentum.fitness.repository;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.fitness.model.Plan;
import com.momentum.fitness.model.enums.PlanType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {
    Optional<Plan> findByOwnerIdAndName(UUID ownerId, String name);
    List<Plan> findAllByOwnerId(UUID ownerId);
    List<Plan> findByIsPublicTrueAndModerationStatus(ModerationStatus status);

    @Query("""
        SELECT DISTINCT p FROM Plan p
        LEFT JOIN FETCH p.sharedUsers su
        LEFT JOIN FETCH p.planDays pd
        WHERE (:query IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:planType IS NULL OR p.type = :planType)
        AND (p.source = 'DEFAULT' OR
             (p.source = 'CUSTOM' AND (p.ownerId = :userId OR p.isPublic = true AND (p.moderationStatus IS NULL OR p.moderationStatus = 'APPROVED'))) OR
             (p.source = 'SHARED' AND EXISTS (SELECT 1 FROM p.sharedUsers u WHERE u.id = :userId)))
        ORDER BY
            CASE
                WHEN :query IS NOT NULL AND LOWER(p.name) = LOWER(:query) THEN 0
                WHEN :query IS NOT NULL AND LOWER(p.name) LIKE LOWER(CONCAT(:query, ' %')) THEN 1
                WHEN :query IS NOT NULL AND LOWER(p.name) LIKE LOWER(CONCAT(:query, '%')) THEN 2
                ELSE 3
            END,
            LENGTH(p.name) ASC,
            CASE WHEN :query IS NOT NULL THEN LOCATE(LOWER(:query), LOWER(p.name)) ELSE 0 END ASC
    """)
    Page<Plan> search(String query, PlanType planType, UUID userId, Pageable pageable);

    @Query("""
        SELECT p FROM Plan p
        LEFT JOIN FETCH p.sharedUsers su
        WHERE p.id = :id
    """)
    Optional<Plan> findByIdWithSharedUsers(UUID id);

    @Query("SELECT COUNT(p) > 0 FROM Plan p WHERE p.id = :planId AND EXISTS (SELECT 1 FROM p.sharedUsers u WHERE u.id = :userId)")
    boolean existsByIdAndSharedUsers_Id(UUID planId, UUID userId);
}

