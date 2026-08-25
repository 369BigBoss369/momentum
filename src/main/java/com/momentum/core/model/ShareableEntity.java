package com.momentum.core.model;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter

@MappedSuperclass
public abstract class ShareableEntity extends OwnableEntity {
    @Builder.Default
    private Boolean isPublic = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ModerationStatus moderationStatus = ModerationStatus.APPROVED;

    @Builder.Default
    @ManyToMany
    protected Set<User> sharedUsers = new HashSet<>();
}


