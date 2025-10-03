package com.digitopia.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateInvitationRequest(
    @NotNull(message = "User ID is required")
    UUID userId,

    @NotNull(message = "Organization ID is required")
    UUID organizationId,

    @NotBlank(message = "Invitation message is required")
    String invitationMessage
) {}

