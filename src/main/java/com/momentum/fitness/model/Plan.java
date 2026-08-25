package com.momentum.fitness.model;

import com.momentum.core.model.ShareableEntity;
import com.momentum.fitness.model.enums.PlanType;
import com.momentum.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

@Entity
@Table(name = "plans")
public class Plan extends ShareableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PlanType type;

    @Builder.Default
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PlanDay> planDays = new ArrayList<>();

    @Override
    @ManyToMany
    @JoinTable(
        name = "plan_shared_users",
        joinColumns = @JoinColumn(name = "plan_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    public Set<User> getSharedUsers() {
        return super.getSharedUsers();
    }
}

