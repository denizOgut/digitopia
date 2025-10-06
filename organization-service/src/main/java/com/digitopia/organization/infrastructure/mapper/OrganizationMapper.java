package com.digitopia.organization.infrastructure.mapper;

import com.digitopia.common.dto.OrganizationDTO;
import com.digitopia.common.mapper.EntityMapper;
import com.digitopia.organization.domain.entity.Organization;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper implements EntityMapper<Organization, OrganizationDTO> {

    @Override
    public OrganizationDTO toDto(Organization entity) {
        if (entity == null) return null;

        return new OrganizationDTO(
            entity.getId(),
            entity.getOrganizationName(),
            entity.getNormalizedOrganizationName(),
            entity.getRegistryNumber(),
            entity.getContactEmail(),
            entity.getCompanySize(),
            entity.getYearFounded(),
            entity.getStatus(),
            entity.getUserIds()
        );
    }

    @Override
    public Organization toEntity(OrganizationDTO dto) {
        if (dto == null) return null;

        var org = new Organization();
        org.setId(dto.id());
        org.setOrganizationName(dto.organizationName());
        org.setNormalizedOrganizationName(dto.normalizedOrganizationName());
        org.setRegistryNumber(dto.registryNumber());
        org.setContactEmail(dto.contactEmail());
        org.setCompanySize(dto.companySize());
        org.setYearFounded(dto.yearFounded());
        org.setStatus(dto.status());
        org.setUserIds(dto.userIds());

        return org;
    }

    @Override
    public void updateEntity(OrganizationDTO dto, Organization entity) {
        if (dto == null || entity == null) return;

        if (dto.organizationName() != null) entity.setOrganizationName(dto.organizationName());
        if (dto.normalizedOrganizationName() != null) entity.setNormalizedOrganizationName(dto.normalizedOrganizationName());
        if (dto.contactEmail() != null) entity.setContactEmail(dto.contactEmail());
        if (dto.companySize() != null) entity.setCompanySize(dto.companySize());
        if (dto.yearFounded() != null) entity.setYearFounded(dto.yearFounded());
        if (dto.status() != null) entity.setStatus(dto.status());
    }
}
