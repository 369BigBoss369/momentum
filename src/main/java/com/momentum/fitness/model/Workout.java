package com.momentum.fitness.model;

import com.momentum.core.model.ShareableEntity;
import com.momentum.fitness.model.enums.WorkoutType;
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
@Table(name = "workouts")
public class Workout extends ShareableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private WorkoutType type;

    @Builder.Default
    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkoutExercise> workoutExercises = new ArrayList<>();

    @Override
    @ManyToMany
    @JoinTable(
        name = "workout_shared_users",
        joinColumns = @JoinColumn(name = "workout_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    public Set<User> getSharedUsers() {
        return super.getSharedUsers();
    }
}

