package com.digitopia.common.dto;

import com.digitopia.common.enums.Role;
import com.digitopia.common.enums.UserStatus;
import java.util.List;
import java.util.UUID;

public record UserDTO(
    UUID id,
    String email,
    UserStatus status,
    String fullName,
    String normalizedName,
    Role role,
    List<UUID> organizationIds
) {
}
