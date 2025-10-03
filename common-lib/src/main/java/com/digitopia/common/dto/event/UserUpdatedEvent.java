package com.digitopia.common.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserUpdatedEvent(
    UUID eventId,
    LocalDateTime timestamp,
    UUID triggeredBy,
    UUID userId,
    String fieldName,
    Object oldValue,
    Object newValue
) {
    public static UserUpdatedEvent create(
        UUID userId,
        String fieldName,
        Object oldValue,
        Object newValue,
        UUID triggeredBy
    ) {
        return new UserUpdatedEvent(
            UUID.randomUUID(),
            LocalDateTime.now(),
            triggeredBy,
            userId,
            fieldName,
            oldValue,
            newValue
        );
    }
}
