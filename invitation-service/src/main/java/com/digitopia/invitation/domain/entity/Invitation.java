package com.digitopia.invitation.domain.entity;

import com.digitopia.common.constants.AppConstants;
import com.digitopia.common.entity.BaseEntity;
import com.digitopia.common.enums.InvitationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Invitation entity representing invitation from organization to user.
 */
@Entity
@Table(name = "invitations", indexes = {
    @Index(name = "idx_invitation_user", columnList = "user_id"),
    @Index(name = "idx_invitation_org", columnList = "organization_id"),
    @Index(name = "idx_invitation_status", columnList = "status")
}, uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_user_org_pending",
        columnNames = {"user_id", "organization_id", "status"}
    )
})
public class Invitation extends BaseEntity {

    /**
     * User being invited (UUID reference).
     */
    @Column(nullable = false, name = "user_id")
    private UUID userId;

    /**
     * Organization sending invitation (UUID reference).
     */
    @Column(nullable = false, name = "organization_id")
    private UUID organizationId;

    /**
     * Invitation message.
     */
    @Column(nullable = false, name = "invitation_message", length = 500)
    private String invitationMessage;

    /**
     * Invitation status.
     * PENDING: Awaiting response
     * ACCEPTED: User joined
     * REJECTED: User declined
     * EXPIRED: Auto-expired after 7 days
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }

    public String getInvitationMessage() { return invitationMessage; }
    public void setInvitationMessage(String invitationMessage) { this.invitationMessage = invitationMessage; }

    public InvitationStatus getStatus() { return status; }
    public void setStatus(InvitationStatus status) { this.status = status; }


    public boolean isExpired() {
        return this.getCreatedAt() != null &&
            this.getCreatedAt().plusDays(AppConstants.INVITATION_EXPIRY_DAYS).isBefore(LocalDateTime.now()) &&
            this.status == InvitationStatus.PENDING;
    }
}
