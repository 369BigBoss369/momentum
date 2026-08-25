package com.momentum.user.repository;

import com.momentum.user.model.FitnessActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FitnessActivityRepository extends JpaRepository<FitnessActivity, UUID> {
}



















