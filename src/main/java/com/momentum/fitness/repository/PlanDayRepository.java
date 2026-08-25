package com.momentum.fitness.repository;

import com.momentum.fitness.model.PlanDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlanDayRepository extends JpaRepository<PlanDay, UUID> {
    @Query("SELECT pd FROM PlanDay pd WHERE pd.plan.id = :planId ORDER BY pd.dayNumber ASC")
    List<PlanDay> getByPlan_Id(UUID planId);

    @Query("SELECT COUNT(pd) FROM PlanDay pd WHERE pd.plan.id = :planId")
    long countByPlanId(@Param("planId") UUID planId);

    @Query("SELECT pd FROM PlanDay pd WHERE pd.plan.id = :planId AND pd.dayNumber = :dayNumber")
    List<PlanDay> findByPlanIdAndDayNumber(@Param("planId") UUID planId, @Param("dayNumber") int dayNumber);

    @Query("SELECT COUNT(pd) FROM PlanDay pd")
    long countAll();
}

