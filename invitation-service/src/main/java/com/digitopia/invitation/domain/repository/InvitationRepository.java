package com.digitopia.invitation.domain.repository;

import com.digitopia.common.enums.InvitationStatus;
import com.digitopia.invitation.domain.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    boolean existsByUserIdAndOrganizationIdAndStatus(UUID userId, UUID organizationId, InvitationStatus status);

    Optional<Invitation> findFirstByUserIdAndOrganizationIdOrderByCreatedAtDesc(UUID userId, UUID organizationId);

    List<Invitation> findByUserIdAndStatus(UUID userId, InvitationStatus status);

    List<Invitation> findByOrganizationIdAndStatus(UUID organizationId, InvitationStatus status);

    @Query("SELECT i FROM Invitation i WHERE i.status = 'PENDING' AND i.createdAt < :expiryDate")
    List<Invitation> findExpiredInvitations(LocalDateTime expiryDate);
}
