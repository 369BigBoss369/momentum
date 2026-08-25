package com.momentum.fitness.model;

import com.momentum.core.model.ShareableEntity;
import com.momentum.fitness.model.enums.ExerciseType;
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
@Table(name = "exercises")
public class Exercise extends ShareableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExerciseType type;
    @Builder.Default
    @ElementCollection
    private List<MuscleTarget> muscleGroupTarget = new ArrayList<>();

    private Integer reps;
    private Integer sets;
    private Double weight;
    private Integer duration;
    private Double burnedCalories;

    @Column(length = 1024)
    private String imageUrl;
    @Column(length = 1024)
    private String videoUrl;

    @Override
    @ManyToMany
    @JoinTable(
        name = "exercise_shared_users",
        joinColumns = @JoinColumn(name = "exercise_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    public Set<User> getSharedUsers() {
        return super.getSharedUsers();
    }
}

