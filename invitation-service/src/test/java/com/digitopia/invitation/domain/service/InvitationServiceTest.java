package com.digitopia.invitation.domain.service;

import com.digitopia.common.constants.AppConstants;
import com.digitopia.common.dto.InvitationDTO;
import com.digitopia.common.dto.request.CreateInvitationRequest;
import com.digitopia.common.enums.InvitationStatus;
import com.digitopia.common.exception.BusinessRuleException;
import com.digitopia.common.exception.ResourceNotFoundException;
import com.digitopia.invitation.domain.entity.Invitation;
import com.digitopia.invitation.domain.repository.InvitationRepository;
import com.digitopia.invitation.infrastructure.mapper.InvitationMapper;
import com.digitopia.invitation.infrastructure.messaging.InvitationEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("InvitationService Tests")
class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private InvitationMapper invitationMapper;

    @Mock
    private InvitationEventPublisher eventPublisher;

    @InjectMocks
    private InvitationService invitationService;

    @Test
    @DisplayName("Should create invitation successfully")
    void shouldCreateInvitation() {
        var request = new CreateInvitationRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Welcome to our organization!"
        );
        var currentUserId = UUID.randomUUID();

        when(invitationRepository.existsByUserIdAndOrganizationIdAndStatus(
            any(), any(), eq(InvitationStatus.PENDING))).thenReturn(false);
        when(invitationRepository.findFirstByUserIdAndOrganizationIdOrderByCreatedAtDesc(any(), any()))
            .thenReturn(Optional.empty());
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(i -> i.getArgument(0));
        when(invitationMapper.toDto(any(Invitation.class))).thenReturn(createMockInvitationDTO());

        var result = invitationService.createInvitation(request, currentUserId);

        assertThat(result).isNotNull();
        verify(invitationRepository).save(any(Invitation.class));
    }

    @Test
    @DisplayName("Should throw exception when pending invitation exists")
    void shouldThrowExceptionWhenPendingExists() {
        var request = new CreateInvitationRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Welcome message"
        );

        when(invitationRepository.existsByUserIdAndOrganizationIdAndStatus(
            any(), any(), eq(InvitationStatus.PENDING))).thenReturn(true);

        assertThatThrownBy(() -> invitationService.createInvitation(request, UUID.randomUUID()))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Pending invitation already exists");

        verify(invitationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when last invitation was rejected")
    void shouldThrowExceptionForRejectedInvitation() {
        var userId = UUID.randomUUID();
        var orgId = UUID.randomUUID();
        var request = new CreateInvitationRequest(userId, orgId, "Welcome message");

        var rejectedInvitation = new Invitation();
        rejectedInvitation.setId(UUID.randomUUID());
        rejectedInvitation.setUserId(userId);
        rejectedInvitation.setOrganizationId(orgId);
        rejectedInvitation.setStatus(InvitationStatus.REJECTED);

        when(invitationRepository.existsByUserIdAndOrganizationIdAndStatus(userId, orgId, InvitationStatus.PENDING))
            .thenReturn(false);
        when(invitationRepository.findFirstByUserIdAndOrganizationIdOrderByCreatedAtDesc(userId, orgId))
            .thenReturn(Optional.of(rejectedInvitation));

        assertThatThrownBy(() -> invitationService.createInvitation(request, UUID.randomUUID()))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Cannot reinvite user who rejected");

        verify(invitationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should accept invitation successfully")
    void shouldAcceptInvitation() {
        var invitationId = UUID.randomUUID();
        var currentUserId = UUID.randomUUID();
        var invitation = createMockInvitation();
        invitation.setStatus(InvitationStatus.PENDING);

        when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(invitation.isExpired()).thenReturn(false);
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(i -> i.getArgument(0));
        when(invitationMapper.toDto(any(Invitation.class))).thenReturn(createMockInvitationDTO());

        var result = invitationService.acceptInvitation(invitationId, currentUserId);

        assertThat(result).isNotNull();
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        verify(eventPublisher).publishInvitationAccepted(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw exception when accepting non-pending invitation")
    void shouldThrowExceptionForNonPendingInvitation() {
        var invitationId = UUID.randomUUID();
        var invitation = new Invitation();
        invitation.setId(invitationId);
        invitation.setStatus(InvitationStatus.ACCEPTED);

        when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.acceptInvitation(invitationId, UUID.randomUUID()))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Only PENDING invitations can be accepted");

        verify(eventPublisher, never()).publishInvitationAccepted(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should reject invitation successfully")
    void shouldRejectInvitation() {
        var invitationId = UUID.randomUUID();
        var currentUserId = UUID.randomUUID();

        var invitation = new Invitation();
        invitation.setId(invitationId);
        invitation.setUserId(UUID.randomUUID());
        invitation.setOrganizationId(UUID.randomUUID());
        invitation.setStatus(InvitationStatus.PENDING);

        when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(i -> i.getArgument(0));
        when(invitationMapper.toDto(any(Invitation.class))).thenReturn(createMockInvitationDTO());

        var result = invitationService.rejectInvitation(invitationId, currentUserId);

        assertThat(result).isNotNull();
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.REJECTED);
        assertThat(invitation.getUpdatedBy()).isEqualTo(currentUserId);
        verify(invitationRepository).save(invitation);
    }

    @Test
    @DisplayName("Should get invitation by ID")
    void shouldGetInvitationById() {
        var invitationId = UUID.randomUUID();
        var invitation = new Invitation();
        invitation.setId(invitationId);
        invitation.setUserId(UUID.randomUUID());
        invitation.setOrganizationId(UUID.randomUUID());
        invitation.setStatus(InvitationStatus.PENDING);

        var expectedDTO = createMockInvitationDTO();

        when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(invitationMapper.toDto(invitation)).thenReturn(expectedDTO);

        var result = invitationService.getInvitationById(invitationId);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedDTO);
        verify(invitationRepository).findById(invitationId);
        verify(invitationMapper).toDto(invitation);
    }

    @Test
    @DisplayName("Should throw exception when invitation not found")
    void shouldThrowExceptionWhenNotFound() {
        var invitationId = UUID.randomUUID();

        when(invitationRepository.findById(invitationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.getInvitationById(invitationId))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get user invitations")
    void shouldGetUserInvitations() {
        var userId = UUID.randomUUID();

        var invitation1 = new Invitation();
        invitation1.setId(UUID.randomUUID());
        invitation1.setUserId(userId);
        invitation1.setStatus(InvitationStatus.PENDING);

        var invitation2 = new Invitation();
        invitation2.setId(UUID.randomUUID());
        invitation2.setUserId(userId);
        invitation2.setStatus(InvitationStatus.PENDING);

        var invitations = List.of(invitation1, invitation2);
        var dto1 = createMockInvitationDTO();
        var dto2 = createMockInvitationDTO();

        when(invitationRepository.findByUserIdAndStatus(userId, InvitationStatus.PENDING))
            .thenReturn(invitations);
        when(invitationMapper.toDto(invitation1)).thenReturn(dto1);
        when(invitationMapper.toDto(invitation2)).thenReturn(dto2);

        var result = invitationService.getUserInvitations(userId, InvitationStatus.PENDING);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(dto1, dto2);
        verify(invitationRepository).findByUserIdAndStatus(userId, InvitationStatus.PENDING);
    }

    @Test
    @DisplayName("Should get organization invitations")
    void shouldGetOrganizationInvitations() {
        var orgId = UUID.randomUUID();

        var invitation1 = new Invitation();
        invitation1.setId(UUID.randomUUID());
        invitation1.setOrganizationId(orgId);
        invitation1.setStatus(InvitationStatus.PENDING);

        var invitation2 = new Invitation();
        invitation2.setId(UUID.randomUUID());
        invitation2.setOrganizationId(orgId);
        invitation2.setStatus(InvitationStatus.PENDING);

        var invitations = List.of(invitation1, invitation2);
        var dto1 = createMockInvitationDTO();
        var dto2 = createMockInvitationDTO();

        when(invitationRepository.findByOrganizationIdAndStatus(orgId, InvitationStatus.PENDING))
            .thenReturn(invitations);
        when(invitationMapper.toDto(invitation1)).thenReturn(dto1);
        when(invitationMapper.toDto(invitation2)).thenReturn(dto2);

        var result = invitationService.getOrganizationInvitations(orgId, InvitationStatus.PENDING);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(dto1, dto2);
        verify(invitationRepository).findByOrganizationIdAndStatus(orgId, InvitationStatus.PENDING);
    }

    @Test
    @DisplayName("Should expire old invitations")
    void shouldExpireOldInvitations() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();

        var invitation1 = new Invitation();
        invitation1.setId(id1);
        invitation1.setStatus(InvitationStatus.PENDING);
        invitation1.setCreatedAt(LocalDateTime.now().minusDays(10));

        var invitation2 = new Invitation();
        invitation2.setId(id2);
        invitation2.setStatus(InvitationStatus.PENDING);
        invitation2.setCreatedAt(LocalDateTime.now().minusDays(8));

        when(invitationRepository.findExpiredInvitations(any(LocalDateTime.class)))
            .thenReturn(List.of(invitation1, invitation2));
        when(invitationRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        invitationService.expireOldInvitations();

        assertThat(invitation1.getStatus()).isEqualTo(InvitationStatus.EXPIRED);
        assertThat(invitation2.getStatus()).isEqualTo(InvitationStatus.EXPIRED);
        verify(invitationRepository).saveAll(anyList());
        verify(eventPublisher).publishInvitationsExpired(anyList(), eq(AppConstants.SYSTEM_USER_ID));
    }

    private Invitation createMockInvitation() {
        var invitation = spy(new Invitation());
        when(invitation.getId()).thenReturn(UUID.randomUUID());
        when(invitation.getUserId()).thenReturn(UUID.randomUUID());
        when(invitation.getOrganizationId()).thenReturn(UUID.randomUUID());
        return invitation;
    }

    private InvitationDTO createMockInvitationDTO() {
        return new InvitationDTO(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Welcome message",
            InvitationStatus.PENDING,
            LocalDateTime.now(),
            LocalDateTime.now(),
            UUID.randomUUID(),
            UUID.randomUUID()
        );
    }
}