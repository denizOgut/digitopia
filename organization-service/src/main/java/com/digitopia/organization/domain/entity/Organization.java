package com.digitopia.organization.domain.entity;

import com.digitopia.common.entity.BaseEntity;
import com.digitopia.common.enums.OrganizationStatus;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Organization entity representing a company/organization.
 * Extends BaseEntity for audit fields.
 */
@Entity
@Table(name = "organizations", indexes = {
    @Index(name = "idx_org_registry", columnList = "registry_number", unique = true),
    @Index(name = "idx_org_normalized_name", columnList = "normalized_name"),
    @Index(name = "idx_org_year", columnList = "year_founded"),
    @Index(name = "idx_org_size", columnList = "company_size")
})
public class Organization extends BaseEntity {

    /**
     * Organization name (alphanumeric).
     */
    @Column(nullable = false, name = "organization_name")
    private String organizationName;

    /**
     * Normalized name for searching (lowercase, ASCII).
     */
    @Column(nullable = false, name = "normalized_name")
    private String normalizedOrganizationName;

    /**
     * Unique registry number (like tax ID).
     * Can only be used once.
     */
    @Column(nullable = false, unique = true, name = "registry_number")
    private String registryNumber;

    /**
     * Contact email for organization.
     */
    @Column(nullable = false, name = "contact_email")
    private String contactEmail;

    /**
     * Organization status.
     * ACTIVE: Organization is operational
     * DELETED: Soft-deleted
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganizationStatus status = OrganizationStatus.ACTIVE;

    /**
     * Number of employees.
     */
    @Column(nullable = false, name = "company_size")
    private Integer companySize;

    /**
     * Year organization was founded.
     */
    @Column(nullable = false, name = "year_founded")
    private Integer yearFounded;

    /**
     * List of user IDs belonging to this organization.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "organization_users",
        joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "user_id")
    private List<UUID> userIds = new ArrayList<>();

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getNormalizedOrganizationName() { return normalizedOrganizationName; }
    public void setNormalizedOrganizationName(String normalizedOrganizationName) {
        this.normalizedOrganizationName = normalizedOrganizationName;
    }

    public String getRegistryNumber() { return registryNumber; }
    public void setRegistryNumber(String registryNumber) { this.registryNumber = registryNumber; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public Integer getCompanySize() { return companySize; }
    public void setCompanySize(Integer companySize) { this.companySize = companySize; }

    public Integer getYearFounded() { return yearFounded; }
    public void setYearFounded(Integer yearFounded) { this.yearFounded = yearFounded; }

    public List<UUID> getUserIds() { return userIds; }
    public void setUserIds(List<UUID> userIds) { this.userIds = userIds; }

    public OrganizationStatus getStatus() { return status; }
    public void setStatus(OrganizationStatus status) { this.status = status; }
}
