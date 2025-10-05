package com.digitopia.user.infrastructure.mapper;

import com.digitopia.common.dto.UserDTO;
import com.digitopia.common.mapper.EntityMapper;
import com.digitopia.user.domain.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper implements EntityMapper<User, UserDTO> {

    @Override
    public UserDTO toDto(User entity)
    {
        if (entity == null)
            return null;

        return new UserDTO(
            entity.getId(),
            entity.getEmail(),
            entity.getStatus(),
            entity.getFullName(),
            entity.getNormalizedName(),
            entity.getRole(),
            entity.getOrganizationIds()
        );
    }

    @Override
    public User toEntity(UserDTO dto)
    {
        if (dto == null)
            return null;

        var user = new User();
        user.setId(dto.id());
        user.setEmail(dto.email());
        user.setStatus(dto.status());
        user.setFullName(dto.fullName());
        user.setNormalizedName(dto.normalizedName());
        user.setRole(dto.role());
        user.setOrganizationIds(dto.organizationIds());
        return user;
    }

    @Override
    public void updateEntity(UserDTO dto, User entity)
    {
        if (dto == null || entity == null)
            return;

        if (dto.email() != null)
            entity.setEmail(dto.email());
        if (dto.status() != null)
            entity.setStatus(dto.status());
        if (dto.fullName() != null)
            entity.setFullName(dto.fullName());
        if (dto.normalizedName() != null)
            entity.setNormalizedName(dto.normalizedName());
        if (dto.role() != null)
            entity.setRole(dto.role());
        if (dto.organizationIds() != null)
            entity.setOrganizationIds(dto.organizationIds());
    }
}
