package com.momentum.nutrition.model;

import com.momentum.nutrition.model.enums.CompositeFoodType;
import com.momentum.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter



@Entity
@Table(name = "composite_foods")
public class CompositeFood extends Food {
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CompositeFoodType type;

    @Override
    @ManyToMany
    @JoinTable(
        name = "composite_food_shared_users",
        joinColumns = @JoinColumn(name = "composite_food_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    public Set<User> getSharedUsers() {
        return super.getSharedUsers();
    }
}

