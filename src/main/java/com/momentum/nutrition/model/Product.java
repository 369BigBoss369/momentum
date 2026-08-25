package com.momentum.nutrition.model;

import com.momentum.nutrition.model.enums.ProductType;
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
@Table(name = "products")
public class Product extends Food {
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductType type;

    @Override
    @ManyToMany
    @JoinTable(
        name = "product_shared_users",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    public Set<User> getSharedUsers() {
        return super.getSharedUsers();
    }
}

