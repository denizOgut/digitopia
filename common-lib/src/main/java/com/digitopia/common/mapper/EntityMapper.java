package com.digitopia.common.mapper;

import com.digitopia.common.entity.BaseEntity;

/**
 * Generic mapper interface for Entity <-> DTO conversion
 */
public interface EntityMapper<E extends BaseEntity, D> {

    /**
     * Convert Entity to DTO
     */
    D toDto(E entity);

    /**
     * Convert DTO to Entity
     */
    E toEntity(D dto);

    /**
     * Update existing entity from DTO
     */
    void updateEntity(D dto, E entity);
}
