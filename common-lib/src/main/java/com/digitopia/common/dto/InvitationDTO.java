package com.digitopia.common.dto;

import com.digitopia.common.enums.InvitationStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvitationDTO(
    UUID id,
    UUID userId,
    UUID organizationId,
    String invitationMessage,
    InvitationStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    UUID createdBy,
    UUID updatedBy
) {
    public boolean isExpired() {
        return createdAt != null &&
            createdAt.plusDays(7).isBefore(LocalDateTime.now()) &&
            status == InvitationStatus.PENDING;
    }

    public boolean canBeReinvited() {
        return status == InvitationStatus.EXPIRED;
    }

    public boolean isPending() {
        return status == InvitationStatus.PENDING;
    }
}
