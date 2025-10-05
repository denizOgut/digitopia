package com.digitopia.user.domain.entity;

import com.digitopia.common.entity.BaseEntity;
import com.digitopia.common.enums.Role;
import com.digitopia.common.enums.UserStatus;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * User entity representing a user in the system.
 * Extends BaseEntity for audit fields (id, createdAt, updatedAt, createdBy, updatedBy).
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_normalized_name", columnList = "normalized_name")
})
public class User extends BaseEntity {

    /**
     * User's email address (unique across all users).
     * Used for authentication and communication.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Hashed password (BCrypt).
     * Never exposed in DTOs or API responses.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Current user status.
     * PENDING: Newly registered, awaiting activation
     * ACTIVE: Can use all features
     * DEACTIVATED: Temporarily disabled
     * DELETED: Soft-deleted (not physically removed)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    /**
     * User's full name (letters and spaces only).
     * Example: "John Doe"
     */
    @Column(nullable = false, name = "full_name")
    private String fullName;

    /**
     * Normalized version of full name for searching.
     * Lowercase, ASCII-only, alphanumeric.
     * Example: "john doe"
     */
    @Column(nullable = false, name = "normalized_name")
    private String normalizedName;

    /**
     * User's role in the system.
     * ADMIN: Full access
     * MANAGER: Cannot delete, creates PENDING users
     * USER: Can only access own resources
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * List of organization IDs this user belongs to.
     * No foreign key - microservices architecture.
     * Actual organization data lives in organization-service.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_organizations",
        joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "organization_id")
    private List<UUID> organizationIds = new ArrayList<>();


    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getNormalizedName() { return normalizedName; }
    public void setNormalizedName(String normalizedName) { this.normalizedName = normalizedName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public List<UUID> getOrganizationIds() { return organizationIds; }
    public void setOrganizationIds(List<UUID> organizationIds) { this.organizationIds = organizationIds; }
}

