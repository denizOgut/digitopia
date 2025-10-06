package com.digitopia.common.dto.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrganizationDeletedEvent(
    UUID eventId,
    LocalDateTime timestamp,
    UUID triggeredBy,
    UUID organizationId,
    List<UUID> deletedUserIds
) {
    public static OrganizationDeletedEvent create(
        UUID organizationId,
        List<UUID> deletedUserIds,
        UUID triggeredBy
    ) {
        return new OrganizationDeletedEvent(
            UUID.randomUUID(),
            LocalDateTime.now(),
            triggeredBy,
            organizationId,
            deletedUserIds
        );
    }
}