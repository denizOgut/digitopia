package com.digitopia.common.dto.request;

import com.digitopia.common.enums.Role;
import jakarta.validation.constraints.*;

public record CreateUserRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Full name is required")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Full name must contain only letters")
    String fullName,

    Role role
) {}


