package com.digitopia.common.dto;

import com.digitopia.common.enums.OrganizationStatus;
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
    OrganizationStatus status,
    List<UUID> userIds
) {}