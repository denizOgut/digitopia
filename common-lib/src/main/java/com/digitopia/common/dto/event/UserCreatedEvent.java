package com.digitopia.common.dto.event;

import com.digitopia.common.dto.UserDTO;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserCreatedEvent(
    UUID eventId,
    LocalDateTime timestamp,
    UUID triggeredBy,
    UserDTO user
) {
    public static UserCreatedEvent create(UserDTO user, UUID triggeredBy) {
        return new UserCreatedEvent(
            UUID.randomUUID(),
            LocalDateTime.now(),
            triggeredBy,
            user
        );
    }
}
