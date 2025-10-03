package com.digitopia.common.dto.request;

import com.digitopia.common.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
    @NotNull(message = "Status is required")
    UserStatus status
) {}


