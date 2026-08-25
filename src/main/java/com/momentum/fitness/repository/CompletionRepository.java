package com.momentum.fitness.repository;

import com.momentum.fitness.model.Completion;
import com.momentum.fitness.model.enums.CompletionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CompletionRepository extends JpaRepository<Completion, UUID> {
    long countByUserId(UUID userId);
    long countByUserIdAndType(UUID userId, CompletionType type);
    List<Completion> findByUserIdAndType(UUID userId, CompletionType type);

    @Query("SELECT c FROM Completion c WHERE c.user.id = :userId ORDER BY c.completedAt DESC")
    List<Completion> findByUserIdOrderByCompletedAtDesc(@Param("userId") UUID userId, @Param("limit") int limit);

    @Query("SELECT COUNT(c) > 0 FROM Completion c WHERE c.user.id = :userId AND c.targetId = :targetId AND c.type = :type")
    boolean existsByUserIdAndTargetIdAndType(@Param("userId") UUID userId, @Param("targetId") UUID targetId, @Param("type") CompletionType type);

    @Query("SELECT COUNT(c) > 0 FROM Completion c WHERE c.user.id = :userId AND c.targetId = :targetId AND c.type = :type AND c.planDayId = :planDayId")
    boolean existsByUserIdAndTargetIdAndTypeAndPlanDayId(@Param("userId") UUID userId, @Param("targetId") UUID targetId, @Param("type") CompletionType type, @Param("planDayId") UUID planDayId);

    @Query("SELECT COUNT(c) > 0 FROM Completion c WHERE c.user.id = :userId AND c.targetId = :targetId AND c.type = :type AND c.planDayId = :planDayId AND c.workoutPosition = :workoutPosition")
    boolean existsByUserIdAndTargetIdAndTypeAndPlanDayIdAndWorkoutPosition(@Param("userId") UUID userId, @Param("targetId") UUID targetId, @Param("type") CompletionType type, @Param("planDayId") UUID planDayId, @Param("workoutPosition") Integer workoutPosition);

    @Query("SELECT COUNT(c) FROM Completion c WHERE c.user.id = :userId AND c.targetId = :targetId AND c.type = :type AND c.planDayId IS NULL")
    long countByUserIdAndTargetIdAndTypeAndPlanDayIdIsNull(@Param("userId") UUID userId, @Param("targetId") UUID targetId, @Param("type") CompletionType type);

    @Query("SELECT COUNT(c) FROM Completion c WHERE c.user.id = :userId AND c.targetId = :targetId AND c.type = :type AND c.planDayId = :planDayId")
    long countByUserIdAndTargetIdAndTypeAndPlanDayId(@Param("userId") UUID userId, @Param("targetId") UUID targetId, @Param("type") CompletionType type, @Param("planDayId") UUID planDayId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Completion c WHERE c.user.id = :userId AND c.targetId = :targetId AND c.type = :type AND c.planDayId = :planDayId AND c.workoutPosition = :workoutPosition")
    void deleteByUserIdAndTargetIdAndTypeAndPlanDayIdAndWorkoutPosition(@Param("userId") UUID userId, @Param("targetId") UUID targetId, @Param("type") CompletionType type, @Param("planDayId") UUID planDayId, @Param("workoutPosition") Integer workoutPosition);

    @Modifying
    @Transactional
    void deleteByUserIdAndTargetIdAndType(UUID userId, UUID targetId, CompletionType type);

    @Modifying
    @Transactional
    @Query("DELETE FROM Completion c WHERE c.user.id = :userId AND c.targetId = :targetId AND c.type = :type AND c.planDayId = :planDayId")
    void deleteByUserIdAndTargetIdAndTypeAndPlanDayId(@Param("userId") UUID userId, @Param("targetId") UUID targetId, @Param("type") CompletionType type, @Param("planDayId") UUID planDayId);


    @Query("SELECT COUNT(c) FROM Completion c WHERE c.user.id = :userId AND c.type = :type AND c.completedAt >= :startDate")
    long countByUserIdAndTypeAndCompletedAtAfter(@Param("userId") UUID userId, @Param("type") CompletionType type, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(c) FROM Completion c WHERE c.user.id = :userId AND c.completedAt BETWEEN :startDate AND :endDate")
    long countByUserIdAndCompletedAtBetween(@Param("userId") UUID userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c FROM Completion c WHERE c.user.id = :userId AND c.type = :type ORDER BY c.completedAt DESC")
    List<Completion> findByUserIdAndTypeOrderByCompletedAtDesc(@Param("userId") UUID userId, @Param("type") CompletionType type, @Param("limit") int limit);

    @Query("SELECT c FROM Completion c WHERE c.user.id = :userId AND c.type = :type AND c.completedAt >= :completedAtAfter")
    List<Completion> findByUserIdAndTypeAndCompletedAtAfter(@Param("userId") UUID userId, @Param("type") CompletionType type, @Param("completedAtAfter") LocalDateTime completedAtAfter);
}