package com.momentum.util;

import com.momentum.core.model.ShareableEntity;
import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.fitness.model.enums.SourceType;
import com.momentum.user.model.User;
import com.momentum.user.model.enums.UserRole;

public class AccessControlUtil {

    public static boolean canView(ShareableEntity entity, User user) {
        if (entity.getSource() == SourceType.DEFAULT) {
            return true;
        }
        if (user.getId().equals(entity.getOwnerId())) {
            return true;
        }
        if (Boolean.TRUE.equals(entity.getIsPublic()) && ModerationUtil.isVisible(entity)) {
            return true;
        }

        if (user.getRole() == UserRole.ADMIN
                && Boolean.TRUE.equals(entity.getIsPublic())
                && entity.getModerationStatus() == ModerationStatus.PENDING) {
            return true;
        }
        return entity.getSharedUsers() != null && entity.getSharedUsers().stream().anyMatch(u -> u.getId().equals(user.getId()));
    }
}