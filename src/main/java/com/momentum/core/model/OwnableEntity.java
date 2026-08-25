package com.momentum.core.model;

import com.momentum.fitness.model.enums.SourceType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter

@MappedSuperclass
public abstract class OwnableEntity {
    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SourceType source;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

