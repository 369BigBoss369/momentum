package com.momentum.fitness.repository;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.fitness.model.Workout;
import com.momentum.fitness.model.WorkoutExercise;
import com.momentum.fitness.model.enums.MuscleType;
import com.momentum.fitness.model.enums.WorkoutType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, UUID> {
    Optional<Workout> findByOwnerIdAndName(UUID ownerId, String name);
    List<Workout> findByIsPublicTrueAndModerationStatus(ModerationStatus status);

    @Query("""
        SELECT w FROM Workout w
        LEFT JOIN FETCH w.workoutExercises we
        LEFT JOIN FETCH we.exercise e
        WHERE w.id = :id
    """)
    Optional<Workout> findByIdWithExercises(UUID id, UUID userId);

    @Query("""
        SELECT w FROM Workout w
        LEFT JOIN FETCH w.workoutExercises we
        LEFT JOIN FETCH we.exercise e
        WHERE (w.source = 'DEFAULT' OR
               (w.source = 'CUSTOM' AND w.ownerId = :userId) OR
               (w.source = 'SHARED' AND EXISTS (SELECT 1 FROM w.sharedUsers u WHERE u.id = :userId)))
    """)
    List<Workout> findAllAccessible(UUID userId);

    @Query("""
        SELECT w FROM Workout w
        LEFT JOIN FETCH w.sharedUsers su
        WHERE LOWER(w.name) LIKE LOWER(CONCAT('%', :query, '%')) AND (
            w.source = 'DEFAULT' OR
            (w.source = 'CUSTOM' AND (w.ownerId = :userId OR w.isPublic = true AND (w.moderationStatus IS NULL OR w.moderationStatus = 'APPROVED'))) OR
            (w.source = 'SHARED' AND EXISTS (SELECT 1 FROM w.sharedUsers u WHERE u.id = :userId))
        )
        ORDER BY w.name ASC
    """)
    List<Workout> searchByName(String query, UUID userId);

    @Query("""
        SELECT w FROM Workout w
        LEFT JOIN FETCH w.sharedUsers su
        WHERE (:query IS NULL OR LOWER(w.name) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:workoutType IS NULL OR w.type = :workoutType)
        AND (w.source = 'DEFAULT' OR
             (w.source = 'CUSTOM' AND (w.ownerId = :userId OR w.isPublic = true AND (w.moderationStatus IS NULL OR w.moderationStatus = 'APPROVED'))) OR
             (w.source = 'SHARED' AND EXISTS (SELECT 1 FROM w.sharedUsers u WHERE u.id = :userId)))
        ORDER BY
            CASE
                WHEN :query IS NOT NULL AND LOWER(w.name) = LOWER(:query) THEN 0
                WHEN :query IS NOT NULL AND LOWER(w.name) LIKE LOWER(CONCAT(:query, ' %')) THEN 1
                WHEN :query IS NOT NULL AND LOWER(w.name) LIKE LOWER(CONCAT(:query, '%')) THEN 2
                ELSE 3
            END,
            LENGTH(w.name) ASC,
            CASE WHEN :query IS NOT NULL THEN LOCATE(LOWER(:query), LOWER(w.name)) ELSE 0 END ASC
    """)
    Page<Workout> search(String query, WorkoutType workoutType, UUID userId, Pageable pageable);

    @Query("""
        SELECT we FROM WorkoutExercise we
        LEFT JOIN FETCH we.exercise e
        LEFT JOIN FETCH e.muscleGroupTarget mgt
        WHERE we.workout.id = :workoutId
        ORDER BY we.number
    """)
    List<WorkoutExercise> findExercisesByWorkoutId(UUID workoutId);

    @Query("""
        SELECT we FROM WorkoutExercise we
        LEFT JOIN FETCH we.exercise e
        LEFT JOIN FETCH e.muscleGroupTarget mgt
        WHERE we.workout.id IN :workoutIds
        ORDER BY we.workout.id, we.number
    """)
    List<WorkoutExercise> findExercisesByWorkoutIds(List<UUID> workoutIds);

    @Query("""
        SELECT w FROM Workout w
        LEFT JOIN FETCH w.sharedUsers su
        WHERE w.id = :id
    """)
    Optional<Workout> findByIdWithSharedUsers(UUID id);

    @Query("SELECT COUNT(w) > 0 FROM Workout w WHERE w.id = :workoutId AND EXISTS (SELECT 1 FROM w.sharedUsers u WHERE u.id = :userId)")
    boolean existsByIdAndSharedUsers_Id(UUID workoutId, UUID userId);

    @Query("""
        SELECT DISTINCT w FROM Workout w
        JOIN w.workoutExercises we
        JOIN we.exercise e
        JOIN e.muscleGroupTarget mgt
        WHERE (w.source = 'DEFAULT' OR
               (w.source = 'CUSTOM' AND w.ownerId = :userId) OR
               (w.source = 'SHARED' AND EXISTS (SELECT 1 FROM w.sharedUsers u WHERE u.id = :userId)))
        AND mgt.muscle IN :muscles
        ORDER BY w.name ASC
    """)
    List<Workout> findAccessibleByMuscleGroups(UUID userId, List<MuscleType> muscles);

    @Query("""
        SELECT w FROM Workout w
        WHERE LOWER(w.name) = LOWER(:name) AND (w.source = 'DEFAULT' OR
               (w.source = 'CUSTOM' AND w.ownerId = :userId) OR
               (w.source = 'SHARED' AND EXISTS (SELECT 1 FROM w.sharedUsers u WHERE u.id = :userId)))
    """)
    Optional<Workout> findAccessibleByName(UUID userId, String name);

}

