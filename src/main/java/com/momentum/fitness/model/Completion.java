package com.momentum.fitness.model;

import com.momentum.user.model.User;
import com.momentum.fitness.model.enums.CompletionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

@Entity
@Table(name = "completions")
public class Completion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CompletionType type;

    @Column(nullable = false)
    private UUID targetId;

    @Column(name = "plan_day_id")
    private UUID planDayId;

    private Integer workoutPosition;

    @CreationTimestamp
    private LocalDateTime completedAt;
}

