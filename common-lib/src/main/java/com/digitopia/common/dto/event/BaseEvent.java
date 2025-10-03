package com.digitopia.common.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record BaseEvent(
    UUID eventId,
    LocalDateTime timestamp,
    UUID triggeredBy
) {
    public static BaseEvent create(UUID triggeredBy) {
        return new BaseEvent(
            UUID.randomUUID(),
            LocalDateTime.now(),
            triggeredBy
        );
    }
}
