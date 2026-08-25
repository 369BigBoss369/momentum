package com.momentum.fitness.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

@Entity
@Table(name = "plan_day_workout_exercises")
public class PlanDayWorkoutExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Integer number;

    private Integer reps;
    private Double weight;
    private Integer duration;

    @Column(nullable = false)
    private Double burnedCalories;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_day_workout_id", nullable = false)
    private PlanDayWorkout planDayWorkout;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;
}






