package com.momentum.user.repository;

import com.momentum.user.model.WaterIntake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface WaterIntakeRepository extends JpaRepository<WaterIntake, UUID> {

    @Query("""
        SELECT COALESCE(SUM(w.amount), 0)
        FROM WaterIntake w
        WHERE w.user.id = :userId AND w.drankAt BETWEEN :start AND :end
        """)
    Double sumAmountByUserAndDrankAtBetween(@Param("userId") UUID userId,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);
}

