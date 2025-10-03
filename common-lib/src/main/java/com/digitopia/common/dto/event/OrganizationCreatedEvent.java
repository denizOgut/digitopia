package com.digitopia.common.dto.event;

import com.digitopia.common.dto.OrganizationDTO;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrganizationCreatedEvent(
    UUID eventId,
    LocalDateTime timestamp,
    UUID triggeredBy,
    OrganizationDTO organization
) {
    public static OrganizationCreatedEvent create(
        OrganizationDTO organization,
        UUID triggeredBy
    ) {
        return new OrganizationCreatedEvent(
            UUID.randomUUID(),
            LocalDateTime.now(),
            triggeredBy,
            organization
        );
    }
}
