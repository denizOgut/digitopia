package com.digitopia.common.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record InvitationAcceptedEvent(
    UUID eventId,
    LocalDateTime timestamp,
    UUID triggeredBy,
    UUID userId,
    UUID organizationId,
    UUID invitationId
) {
    public static InvitationAcceptedEvent create(
        UUID userId,
        UUID organizationId,
        UUID invitationId,
        UUID triggeredBy
    ) {
        return new InvitationAcceptedEvent(
            UUID.randomUUID(),
            LocalDateTime.now(),
            triggeredBy,
            userId,
            organizationId,
            invitationId
        );
    }
}
