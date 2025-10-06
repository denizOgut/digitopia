package com.digitopia.organization.domain.service;

import com.digitopia.common.constants.AppConstants;
import com.digitopia.common.dto.OrganizationDTO;
import com.digitopia.common.dto.request.CreateOrganizationRequest;
import com.digitopia.common.dto.request.SearchOrganizationRequest;
import com.digitopia.common.enums.OrganizationStatus;
import com.digitopia.common.exception.DuplicateResourceException;
import com.digitopia.common.exception.ResourceNotFoundException;
import com.digitopia.common.util.StringUtils;
import com.digitopia.organization.domain.entity.Organization;
import com.digitopia.organization.domain.repository.OrganizationRepository;
import com.digitopia.organization.infrastructure.mapper.OrganizationMapper;
import com.digitopia.organization.infrastructure.messaging.OrganizationEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing organization operations.
 * Handles CRUD, validation, caching, and event publishing.
 */
@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;
    private final OrganizationEventPublisher eventPublisher;

    private static final Logger log = LoggerFactory.getLogger(OrganizationService.class);

    public OrganizationService(
        OrganizationRepository organizationRepository,
        OrganizationMapper organizationMapper,
        OrganizationEventPublisher eventPublisher
    ) {
        this.organizationRepository = organizationRepository;
        this.organizationMapper = organizationMapper;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new organization with the provided details.
     *
     * <p>This method validates that the registry number is unique before creating
     * the organization. It sanitizes and normalizes input strings, sets audit fields,
     * and publishes an organization created event upon successful creation.</p>
     *
     * @param request the organization creation request containing name, registry number,
     *                contact email, company size, and year founded
     * @param currentUserId the UUID of the user creating the organization
     * @return the created organization as a DTO
     * @throws DuplicateResourceException if an organization with the same registry number already exists
     */
    @Transactional
    public OrganizationDTO createOrganization(CreateOrganizationRequest request, UUID currentUserId) {
        if (organizationRepository.existsByRegistryNumber(request.registryNumber())) {
            throw new DuplicateResourceException("Organization already exists with registry: " + request.registryNumber());
        }

        var org = new Organization();
        org.setOrganizationName(StringUtils.sanitize(request.organizationName()));
        org.setNormalizedOrganizationName(StringUtils.normalizeToAscii(request.organizationName()));
        org.setRegistryNumber(request.registryNumber().trim());
        org.setContactEmail(StringUtils.normalizeEmail(request.contactEmail()));
        org.setCompanySize(request.companySize());
        org.setYearFounded(request.yearFounded());
        org.setStatus(OrganizationStatus.ACTIVE);
        org.setCreatedBy(currentUserId);
        org.setUpdatedBy(currentUserId);

        var saved = organizationRepository.save(org);
        var dto = organizationMapper.toDto(saved);

        eventPublisher.publishOrganizationCreated(dto, currentUserId);

        return dto;
    }

    /**
     * Retrieves an organization by its unique identifier.
     *
     * <p>The result is cached in Redis using the cache name "orgById" with the
     * organization ID as the cache key. Subsequent calls with the same ID will
     * return the cached value without hitting the database.</p>
     *
     * @param id the unique identifier of the organization
     * @return the organization as a DTO
     * @throws ResourceNotFoundException if no organization exists with the given ID
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "orgById", key = "#id")
    public OrganizationDTO getOrganizationById(UUID id) {
        var org = organizationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Organization " + id.toString()));
        return organizationMapper.toDto(org);
    }

    /**
     * Retrieves an organization by its registry number.
     *
     * <p>The result is cached in Redis using the cache name "orgByRegistry" with the
     * registry number as the cache key. This provides fast lookup for organizations
     * by their official registration identifiers.</p>
     *
     * @param registryNumber the registry number of the organization
     * @return the organization as a DTO
     * @throws ResourceNotFoundException if no organization exists with the given registry number
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "orgByRegistry", key = "#registryNumber")
    public OrganizationDTO getByRegistryNumber(String registryNumber) {
        var org = organizationRepository.findByRegistryNumber(registryNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found with registry: " + registryNumber));
        return organizationMapper.toDto(org);
    }

    /**
     * Searches for organizations based on flexible criteria.
     *
     * <p>All search parameters are optional, allowing for flexible queries. The name
     * is normalized to ASCII before searching to support international characters.
     * Results are returned in a pageable format.</p>
     *
     * @param request the search request containing optional criteria:
     *                normalized name, year founded, company size, page number, and page size
     * @return a page of organizations matching the search criteria
     */
    @Transactional(readOnly = true)
    public Page<OrganizationDTO> searchOrganizations(SearchOrganizationRequest request) {
        var pageable = PageRequest.of(request.page(), request.size());

        var normalizedName = request.normalizedName() != null
            ? StringUtils.normalizeToAscii(request.normalizedName())
            : null;

        return organizationRepository.searchOrganizations(
            normalizedName,
            request.yearFounded(),
            request.companySize(),
            pageable
        ).map(organizationMapper::toDto);
    }

    /**
     * Retrieves the list of user IDs belonging to an organization.
     *
     * <p>This method returns only the user IDs, not full user objects, to minimize
     * data transfer and allow the caller to fetch user details as needed.</p>
     *
     * @param organizationId the unique identifier of the organization
     * @return a list of user UUIDs who are members of the organization
     * @throws ResourceNotFoundException if no organization exists with the given ID
     */
    @Transactional(readOnly = true)
    public List<UUID> getOrganizationUsers(UUID organizationId) {
        var org = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization " + organizationId.toString()));
        return org.getUserIds();
    }

    /**
     * Adds a user to an organization's member list.
     *
     * <p>This method is typically called by the invitation service when a user
     * accepts an invitation to join an organization. It ensures idempotency by
     * checking if the user is already a member before adding them.</p>
     *
     * @param organizationId the unique identifier of the organization
     * @param userId the unique identifier of the user to add
     * @throws ResourceNotFoundException if no organization exists with the given ID
     */
    @Transactional
    public void addUserToOrganization(UUID organizationId, UUID userId) {
        var org = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization " + organizationId.toString()));

        if (!org.getUserIds().contains(userId)) {
            org.getUserIds().add(userId);
            organizationRepository.save(org);
        }
    }

    /**
     * Soft-deletes an organization by setting status to DELETED.
     * Organization record remains in database but is marked as deleted..
     *
     * @param id the unique identifier of the organization to delete
     * @param currentUserId the user performing the deletion
     * @throws ResourceNotFoundException if no organization exists with the given ID
     */
    @Transactional
    @CacheEvict(value = {"orgById", "orgByRegistry"}, allEntries = true)
    public void deleteOrganization(UUID id, UUID currentUserId) {
        var org = organizationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Organization "+ id.toString()));

        org.setStatus(OrganizationStatus.DELETED);
        org.setUpdatedBy(currentUserId == null ? AppConstants.SYSTEM_USER_ID : currentUserId);
        organizationRepository.save(org);

        log.info("Organization {} soft-deleted by user {}", id, currentUserId);
    }
}

