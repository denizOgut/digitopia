package com.digitopia.common.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrganizationDTO(
    UUID id,
    String organizationName,
    String normalizedOrganizationName,
    String registryNumber,
    String contactEmail,
    Integer companySize,
    Integer yearFounded,
    List<UUID> userIds
) {}