package com.momentum.util;

import com.momentum.core.model.ShareableEntity;
import com.momentum.core.model.enums.ModerationStatus;

public class ModerationUtil {

    public static void applyPublicityChange(ShareableEntity entity, boolean wasPublic) {
        boolean isNowPublic = Boolean.TRUE.equals(entity.getIsPublic());
        if (isNowPublic && !wasPublic) {
            entity.setModerationStatus(ModerationStatus.PENDING);
        } else if (!isNowPublic) {
            entity.setModerationStatus(ModerationStatus.APPROVED);
        }
    }

    public static boolean isVisible(ShareableEntity entity) {
        return entity.getModerationStatus() == null || entity.getModerationStatus() == ModerationStatus.APPROVED;
    }
}