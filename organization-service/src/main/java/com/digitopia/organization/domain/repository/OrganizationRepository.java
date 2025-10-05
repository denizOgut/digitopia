package com.digitopia.organization.domain.repository;

import com.digitopia.organization.domain.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    Optional<Organization> findByRegistryNumber(String registryNumber);

    boolean existsByRegistryNumber(String registryNumber);

    Page<Organization> findByNormalizedOrganizationNameContaining(String name, Pageable pageable);

    @Query("SELECT o FROM Organization o WHERE " +
        "(:name IS NULL OR o.normalizedOrganizationName LIKE %:name%) AND " +
        "(:year IS NULL OR o.yearFounded = :year) AND " +
        "(:size IS NULL OR o.companySize = :size)")
    Page<Organization> searchOrganizations(
        @Param("name") String name,
        @Param("year") Integer year,
        @Param("size") Integer size,
        Pageable pageable
    );
}