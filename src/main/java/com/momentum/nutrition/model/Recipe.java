package com.momentum.nutrition.model;

import com.momentum.nutrition.dto.BaseFoodData;
import com.momentum.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashSet;
import java.util.Set;

@AllArgsConstructor
@SuperBuilder
@Getter
@Setter



@Entity
@Table(name = "recipes")
public class Recipe extends CompositeFood implements BaseFoodData {
    @Builder.Default
    @OneToMany(mappedBy = "recipe", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RecipeIngredient> ingredients = new LinkedHashSet<>();

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    private Set<Step> steps = new LinkedHashSet<>();

    public Recipe() {
        super();
    }

    @Override
    public Integer getServingSize() {
        return 0;
    }

    @Override
    public void setServingSize(Integer servingSize) {
        return;
    }

    @Override
    @ManyToMany
    @JoinTable(
        name = "recipe_shared_users",
        joinColumns = @JoinColumn(name = "recipe_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    public Set<User> getSharedUsers() {
        return super.getSharedUsers();
    }
}

