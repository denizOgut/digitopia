package com.digitopia.common.dto.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InvitationExpiredEvent(
    UUID eventId,
    LocalDateTime timestamp,
    UUID triggeredBy,
    List<UUID> expiredInvitationIds
) {
    public static InvitationExpiredEvent create(
        List<UUID> expiredIds,
        UUID triggeredBy
    ) {
        return new InvitationExpiredEvent(
            UUID.randomUUID(),
            LocalDateTime.now(),
            triggeredBy,
            expiredIds
        );
    }
}
