package com.digitopia.common.dto.request;

import jakarta.validation.constraints.*;

public record CreateOrganizationRequest(
    @NotBlank(message = "Organization name is required")
    @Pattern(regexp = "^[a-zA-Z0-9\\s]+$", message = "Organization name must be alphanumeric")
    String organizationName,

    @NotBlank(message = "Registry number is required")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Registry number must be alphanumeric")
    String registryNumber,

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    String contactEmail,

    @NotNull(message = "Company size is required")
    @Positive(message = "Company size must be positive")
    Integer companySize,

    @NotNull(message = "Year founded is required")
    @Min(value = 1800, message = "Year must be after 1800")
    @Max(value = 2100, message = "Year must be before 2025")
    Integer yearFounded
) {}


