package com.digitopia.invitation.infrastructure.mapper;

import com.digitopia.common.dto.InvitationDTO;
import com.digitopia.common.mapper.EntityMapper;
import com.digitopia.invitation.domain.entity.Invitation;
import org.springframework.stereotype.Component;

@Component
public class InvitationMapper implements EntityMapper<Invitation, InvitationDTO> {

    @Override
    public InvitationDTO toDto(Invitation entity) {
        if (entity == null) return null;

        return new InvitationDTO(
            entity.getId(),
            entity.getUserId(),
            entity.getOrganizationId(),
            entity.getInvitationMessage(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getCreatedBy(),
            entity.getUpdatedBy()
        );
    }

    @Override
    public Invitation toEntity(InvitationDTO dto) {
        if (dto == null) return null;

        var invitation = new Invitation();
        invitation.setId(dto.id());
        invitation.setUserId(dto.userId());
        invitation.setOrganizationId(dto.organizationId());
        invitation.setInvitationMessage(dto.invitationMessage());
        invitation.setStatus(dto.status());
        invitation.setCreatedAt(dto.createdAt());
        invitation.setUpdatedAt(dto.updatedAt());
        invitation.setCreatedBy(dto.createdBy());
        invitation.setUpdatedBy(dto.updatedBy());

        return invitation;
    }

    @Override
    public void updateEntity(InvitationDTO dto, Invitation entity) {
        if (dto == null || entity == null) return;

        if (dto.status() != null) entity.setStatus(dto.status());
        if (dto.invitationMessage() != null) entity.setInvitationMessage(dto.invitationMessage());
    }
}
