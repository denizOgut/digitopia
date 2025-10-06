package com.digitopia.common.dto.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserDeletedEvent(
    UUID eventId,
    LocalDateTime timestamp,
    UUID triggeredBy,
    UUID userId,
    List<UUID> organizationIds
) {
    public static UserDeletedEvent create(
        UUID userId,
        List<UUID> organizationIds,
        UUID triggeredBy
    ) {
        return new UserDeletedEvent(
            UUID.randomUUID(),
            LocalDateTime.now(),
            triggeredBy,
            userId,
            organizationIds
        );
    }
}
