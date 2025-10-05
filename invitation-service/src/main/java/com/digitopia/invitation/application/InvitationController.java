package com.digitopia.invitation.application;

import com.digitopia.common.constants.AppConstants;
import com.digitopia.common.dto.InvitationDTO;
import com.digitopia.common.dto.request.CreateInvitationRequest;
import com.digitopia.common.enums.InvitationStatus;
import com.digitopia.invitation.domain.service.InvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invitations")
@Tag(name = "Invitation Management", description = "Organization invitation operations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    @Operation(summary = "Create new invitation")
    public ResponseEntity<InvitationDTO> createInvitation(
        @Valid @RequestBody CreateInvitationRequest request,
        @RequestHeader(AppConstants.HEADER_USER_ID) UUID currentUserId
    ) {
        var invitation = invitationService.createInvitation(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(invitation);
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "Accept invitation")
    public ResponseEntity<InvitationDTO> acceptInvitation(
        @PathVariable UUID id,
        @RequestHeader(AppConstants.HEADER_USER_ID) UUID currentUserId
    ) {
        return ResponseEntity.ok(invitationService.acceptInvitation(id, currentUserId));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject invitation")
    public ResponseEntity<InvitationDTO> rejectInvitation(
        @PathVariable UUID id,
        @RequestHeader(AppConstants.HEADER_USER_ID) UUID currentUserId
    ) {
        return ResponseEntity.ok(invitationService.rejectInvitation(id, currentUserId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get invitation by ID")
    public ResponseEntity<InvitationDTO> getInvitation(@PathVariable UUID id) {
        return ResponseEntity.ok(invitationService.getInvitationById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user's invitations")
    public ResponseEntity<List<InvitationDTO>> getUserInvitations(
        @PathVariable UUID userId,
        @RequestParam(defaultValue = "PENDING") InvitationStatus status
    ) {
        return ResponseEntity.ok(invitationService.getUserInvitations(userId, status));
    }

    @GetMapping("/organization/{orgId}")
    @Operation(summary = "Get organization's invitations")
    public ResponseEntity<List<InvitationDTO>> getOrganizationInvitations(
        @PathVariable UUID orgId,
        @RequestParam(defaultValue = "PENDING") InvitationStatus status
    ) {
        return ResponseEntity.ok(invitationService.getOrganizationInvitations(orgId, status));
    }
}
