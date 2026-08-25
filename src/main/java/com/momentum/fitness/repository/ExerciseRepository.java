package com.momentum.fitness.repository;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.fitness.model.Exercise;
import com.momentum.fitness.model.enums.ExerciseType;
import com.momentum.fitness.model.enums.MuscleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {
    Optional<Exercise> findByOwnerIdAndName(UUID ownerId, String name);
    List<Exercise> findByIsPublicTrueAndModerationStatus(ModerationStatus status);

    @Query("""
        SELECT e FROM Exercise e
        LEFT JOIN FETCH e.muscleGroupTarget mgt
        WHERE e.id = :id
    """)
    Optional<Exercise> findByIdWithMuscleTargets(UUID id);

    @Query("""
        SELECT e FROM Exercise e
        LEFT JOIN FETCH e.muscleGroupTarget mgt
        WHERE e.id = :exerciseId
    """)
    Optional<Exercise> findByIdWithMuscleTargetsForViewing(UUID exerciseId);

    @Query("""
        SELECT e FROM Exercise e
        WHERE (e.source = 'DEFAULT' OR
               (e.source = 'CUSTOM' AND e.ownerId = :userId) OR
               EXISTS (SELECT 1 FROM e.sharedUsers u WHERE u.id = :sharedUserId))
        ORDER BY e.name ASC
    """)
    Page<Exercise> findAllAccessible(UUID userId, UUID sharedUserId, Pageable pageable);

    @Query("""
        SELECT e FROM Exercise e
        LEFT JOIN FETCH e.muscleGroupTarget mgt
        WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%'))
        AND (e.source = 'DEFAULT' OR
             (e.source = 'CUSTOM' AND e.ownerId = :userId) OR
             EXISTS (SELECT 1 FROM e.sharedUsers u WHERE u.id = :sharedUserId))
        ORDER BY
            CASE
                WHEN LOWER(e.name) = LOWER(:query) THEN 0
                WHEN LOWER(e.name) LIKE LOWER(CONCAT(:query, ' %')) THEN 1
                WHEN LOWER(e.name) LIKE LOWER(CONCAT(:query, '%')) THEN 2
                ELSE 3
            END,
            LENGTH(e.name) ASC,
            LOCATE(LOWER(:query), LOWER(e.name)) ASC
    """)
    Page<Exercise> searchByName(String query, UUID userId, UUID sharedUserId, Pageable pageable);

    @Query("""
        SELECT e FROM Exercise e
        LEFT JOIN FETCH e.sharedUsers su
        WHERE (:query IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:exerciseType IS NULL OR e.type = :exerciseType)
        AND (e.source = 'DEFAULT' OR
             (e.source = 'CUSTOM' AND (e.ownerId = :userId OR e.isPublic = true AND (e.moderationStatus IS NULL OR e.moderationStatus = 'APPROVED'))) OR
             (e.source = 'SHARED' AND EXISTS (SELECT 1 FROM e.sharedUsers u WHERE u.id = :userId)))
        ORDER BY
            CASE
                WHEN :query IS NOT NULL AND LOWER(e.name) = LOWER(:query) THEN 0
                WHEN :query IS NOT NULL AND LOWER(e.name) LIKE LOWER(CONCAT(:query, ' %')) THEN 1
                WHEN :query IS NOT NULL AND LOWER(e.name) LIKE LOWER(CONCAT(:query, '%')) THEN 2
                ELSE 3
            END,
            LENGTH(e.name) ASC,
            CASE WHEN :query IS NOT NULL THEN LOCATE(LOWER(:query), LOWER(e.name)) ELSE 0 END ASC
    """)
    Page<Exercise> search(String query, ExerciseType exerciseType, UUID userId, Pageable pageable);

    boolean existsByIdAndSharedUsers_Id(UUID exerciseId, UUID sharedUserId);

    @Query("SELECT DISTINCT e FROM Exercise e LEFT JOIN FETCH e.muscleGroupTarget mgt WHERE e.id IN :exerciseIds")
    List<Exercise> findAllByIdWithMuscleGroups(List<UUID> exerciseIds);

    @Query("SELECT e FROM Exercise e LEFT JOIN FETCH e.sharedUsers su WHERE e.id IN :exerciseIds")
    List<Exercise> findAllByIdWithSharedUsers(List<UUID> exerciseIds);

    @Query("""
        SELECT e FROM Exercise e
        LEFT JOIN FETCH e.sharedUsers su
        WHERE e.id = :id
    """)
    Optional<Exercise> findByIdWithSharedUsers(UUID id);

    @Query("""
        SELECT DISTINCT e FROM Exercise e
        JOIN e.muscleGroupTarget mgt
        WHERE (e.source = 'DEFAULT' OR
               (e.source = 'CUSTOM' AND e.ownerId = :userId) OR
               EXISTS (SELECT 1 FROM e.sharedUsers u WHERE u.id = :sharedUserId))
        AND mgt.muscle IN :muscles
        ORDER BY e.name ASC
    """)
    List<Exercise> findAccessibleByMuscleGroups(UUID userId, UUID sharedUserId, List<MuscleType> muscles);

    @Query("""
        SELECT e FROM Exercise e
        WHERE LOWER(e.name) = LOWER(:name) AND (e.source = 'DEFAULT' OR
               (e.source = 'CUSTOM' AND e.ownerId = :userId) OR
               EXISTS (SELECT 1 FROM e.sharedUsers u WHERE u.id = :sharedUserId))
    """)
    Optional<Exercise> findAccessibleByName(UUID userId, UUID sharedUserId, String name);
}

