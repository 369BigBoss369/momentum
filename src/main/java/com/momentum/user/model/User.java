package com.momentum.user.model;

import com.momentum.fitness.model.Plan;
import com.momentum.user.model.enums.AuthProvider;
import com.momentum.user.model.enums.GenderType;
import com.momentum.user.model.enums.UserGoal;
import com.momentum.user.model.enums.UserRole;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(unique = true)
    private String email;

    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;
    @Column(name = "provider_id")
    private String providerId;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private Integer height;
    private Double weight;
    private Integer age;
    private GenderType gender;
    @Enumerated(EnumType.STRING)
    private UserGoal goal;
    private Double targetWeight;
    private Double pace;

    private Integer maxCalories;
    private Integer maxCarbohydrates;
    private Integer maxProtein;
    private Integer maxFat;

    @Builder.Default
    private Integer dailyWaterGoal = 1500;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_plan_id", unique = false)
    private Plan currentPlan;

    @Column(name = "current_plan_start_date")
    private LocalDate currentPlanStartDate;
}

