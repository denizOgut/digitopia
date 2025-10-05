package com.digitopia.invitation.domain.service;

import com.digitopia.common.constants.AppConstants;
import com.digitopia.common.dto.InvitationDTO;
import com.digitopia.common.dto.request.CreateInvitationRequest;
import com.digitopia.common.enums.InvitationStatus;
import com.digitopia.common.exception.BusinessRuleException;
import com.digitopia.common.exception.ResourceNotFoundException;
import com.digitopia.common.util.StringUtils;
import com.digitopia.invitation.domain.entity.Invitation;
import com.digitopia.invitation.domain.repository.InvitationRepository;
import com.digitopia.invitation.infrastructure.mapper.InvitationMapper;
import com.digitopia.invitation.infrastructure.messaging.InvitationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing invitations.
 *
 * <p>This service handles the complete lifecycle of invitations including creation,
 * acceptance, rejection, and expiration. It enforces business rules to ensure
 * invitation integrity and coordinates with other services through event publishing.</p>
 *
 * <p>Business Rules enforced by this service:</p>
 * <ul>
 *   <li>Only one PENDING invitation can exist per user-organization pair</li>
 *   <li>Users cannot be reinvited if their last invitation was REJECTED</li>
 *   <li>Users can be reinvited if their last invitation EXPIRED</li>
 *   <li>Invitations automatically expire after a configured number of days</li>
 *   <li>Only PENDING invitations can be accepted or rejected</li>
 * </ul>
 *
 */
@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final InvitationMapper invitationMapper;
    private final InvitationEventPublisher eventPublisher;

    /**
     * Constructs a new InvitationService with required dependencies.
     *
     * @param invitationRepository the repository for invitation data access
     * @param invitationMapper the mapper for converting between entities and DTOs
     * @param eventPublisher the publisher for invitation-related events
     */
    public InvitationService(
        InvitationRepository invitationRepository,
        InvitationMapper invitationMapper,
        InvitationEventPublisher eventPublisher
    ) {
        this.invitationRepository = invitationRepository;
        this.invitationMapper = invitationMapper;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new invitation for a user to join an organization.
     * @param request the invitation creation request containing userId, organizationId,
     *                and optional invitation message
     * @param currentUserId the UUID of the user creating the invitation
     * @return the created invitation as a DTO
     * @throws BusinessRuleException if a pending invitation already exists,
     *                               or if the last invitation was rejected
     * @throws NullPointerException if request or currentUserId is null
     */
    @Transactional
    public InvitationDTO createInvitation(CreateInvitationRequest request, UUID currentUserId) {
        if (invitationRepository.existsByUserIdAndOrganizationIdAndStatus(
            request.userId(), request.organizationId(), InvitationStatus.PENDING)) {
            throw new BusinessRuleException("Pending invitation already exists for this user and organization");
        }

        var lastInvitation = invitationRepository.findFirstByUserIdAndOrganizationIdOrderByCreatedAtDesc(
            request.userId(), request.organizationId()
        );

        if (lastInvitation.isPresent() && lastInvitation.get().getStatus() == InvitationStatus.REJECTED) {
            throw new BusinessRuleException("Cannot reinvite user who rejected the last invitation");
        }

        var invitation = new Invitation();
        invitation.setUserId(request.userId());
        invitation.setOrganizationId(request.organizationId());
        invitation.setInvitationMessage(StringUtils.sanitize(request.invitationMessage()));
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setCreatedBy(currentUserId);
        invitation.setUpdatedBy(currentUserId);

        var saved = invitationRepository.save(invitation);
        return invitationMapper.toDto(saved);
    }

    /**
     * Accepts a pending invitation.
     * @param invitationId the UUID of the invitation to accept
     * @param currentUserId the UUID of the user accepting the invitation
     * @return the updated invitation as a DTO with ACCEPTED status
     * @throws ResourceNotFoundException if the invitation is not found
     * @throws BusinessRuleException if the invitation is not PENDING or has expired
     * @throws NullPointerException if invitationId or currentUserId is null
     */
    @Transactional
    public InvitationDTO acceptInvitation(UUID invitationId, UUID currentUserId) {
        var invitation = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new ResourceNotFoundException("Invitation " + invitationId.toString()));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING invitations can be accepted");
        }

        if (invitation.isExpired()) {
            throw new BusinessRuleException("Invitation has expired");
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setUpdatedBy(currentUserId);

        var saved = invitationRepository.save(invitation);

        eventPublisher.publishInvitationAccepted(
            invitation.getUserId(),
            invitation.getOrganizationId(),
            invitation.getId(),
            currentUserId
        );

        return invitationMapper.toDto(saved);
    }

    /**
     * Rejects a pending invitation.
     * @param invitationId the UUID of the invitation to reject
     * @param currentUserId the UUID of the user rejecting the invitation
     * @return the updated invitation as a DTO with REJECTED status
     * @throws ResourceNotFoundException if the invitation is not found
     * @throws BusinessRuleException if the invitation is not PENDING
     * @throws NullPointerException if invitationId or currentUserId is null
     */
    @Transactional
    public InvitationDTO rejectInvitation(UUID invitationId, UUID currentUserId) {
        var invitation = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new ResourceNotFoundException("Invitation " + invitationId.toString()));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING invitations can be rejected");
        }

        invitation.setStatus(InvitationStatus.REJECTED);
        invitation.setUpdatedBy(currentUserId);

        var saved = invitationRepository.save(invitation);
        return invitationMapper.toDto(saved);
    }

    /**
     * Retrieves an invitation by its unique identifier.
     *
     * @param id the UUID of the invitation to retrieve
     * @return the invitation as a DTO
     * @throws ResourceNotFoundException if no invitation exists with the given ID
     * @throws NullPointerException if id is null
     */
    @Transactional(readOnly = true)
    public InvitationDTO getInvitationById(UUID id) {
        var invitation = invitationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Invitation " + id.toString()));
        return invitationMapper.toDto(invitation);
    }

    /**
     * Retrieves all invitations for a specific user filtered by status.
     *
     * @param userId the UUID of the user whose invitations to retrieve
     * @param status the invitation status to filter by
     * @return a list of invitation DTOs matching the criteria; empty list if none found
     * @throws NullPointerException if userId or status is null
     */
    @Transactional(readOnly = true)
    public List<InvitationDTO> getUserInvitations(UUID userId, InvitationStatus status) {
        return invitationRepository.findByUserIdAndStatus(userId, status)
            .stream()
            .map(invitationMapper::toDto)
            .toList();
    }

    /**
     * Retrieves all invitations for a specific organization filtered by status.
     * @param organizationId the UUID of the organization whose invitations to retrieve
     * @param status the invitation status to filter by
     * @return a list of invitation DTOs matching the criteria; empty list if none found
     * @throws NullPointerException if organizationId or status is null
     */
    @Transactional(readOnly = true)
    public List<InvitationDTO> getOrganizationInvitations(UUID organizationId, InvitationStatus status) {
        return invitationRepository.findByOrganizationIdAndStatus(organizationId, status)
            .stream()
            .map(invitationMapper::toDto)
            .toList();
    }

    /**
     * Expires old pending invitations that have exceeded the expiry period.
     */
    @Transactional
    public void expireOldInvitations() {
        var expiryDate = LocalDateTime.now().minusDays(AppConstants.INVITATION_EXPIRY_DAYS);
        var expiredInvitations = invitationRepository.findExpiredInvitations(expiryDate);

        if (expiredInvitations.isEmpty()) {
            return;
        }

        var expiredIds = expiredInvitations.stream()
            .map(invitation -> {
                invitation.setStatus(InvitationStatus.EXPIRED);
                return invitation.getId();
            })
            .toList();

        invitationRepository.saveAll(expiredInvitations);

        eventPublisher.publishInvitationsExpired(expiredIds, AppConstants.SYSTEM_USER_ID);
    }
}
