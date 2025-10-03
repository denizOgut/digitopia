package com.digitopia.common.dto;

import com.digitopia.common.enums.Role;
import com.digitopia.common.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserDTO(
    UUID id,
    String email,
    UserStatus status,
    String fullName,
    String normalizedName,
    Role role,
    List<UUID> organizationIds,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    UUID createdBy,
    UUID updatedBy
) {
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean canDelete() {
        return status != UserStatus.DELETED;
    }
}
