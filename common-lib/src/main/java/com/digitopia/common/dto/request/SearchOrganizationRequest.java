package com.digitopia.common.dto.request;

public record SearchOrganizationRequest(
    String normalizedName,
    Integer yearFounded,
    Integer companySize,
    String registryNumber,
    Integer page,
    Integer size
) {
    public SearchOrganizationRequest {
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 20;
        if (size > 100) size = 100;
    }
}

