package com.digitopia.organization.domain.service;

import com.digitopia.common.dto.OrganizationDTO;
import com.digitopia.common.dto.request.CreateOrganizationRequest;
import com.digitopia.common.enums.OrganizationStatus;
import com.digitopia.common.exception.DuplicateResourceException;
import com.digitopia.common.exception.ResourceNotFoundException;
import com.digitopia.organization.domain.entity.Organization;
import com.digitopia.organization.domain.repository.OrganizationRepository;
import com.digitopia.organization.infrastructure.mapper.OrganizationMapper;
import com.digitopia.organization.infrastructure.messaging.OrganizationEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMapper organizationMapper;

    @Mock
    private OrganizationEventPublisher eventPublisher;

    @InjectMocks
    private OrganizationService organizationService;

    @Test
    @DisplayName("Should create organization successfully")
    void shouldCreateOrganization() {
        var request = new CreateOrganizationRequest(
            "Tech Corp", "REG12345", "contact@techcorp.com", 100, 2020
        );
        var currentUserId = UUID.randomUUID();

        when(organizationRepository.existsByRegistryNumber(anyString())).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenAnswer(i -> i.getArgument(0));
        when(organizationMapper.toDto(any(Organization.class))).thenReturn(createMockOrgDTO());

        var result = organizationService.createOrganization(request, currentUserId);

        assertThat(result).isNotNull();
        assertThat(result.organizationName()).isEqualTo("Tech Corp");
        verify(organizationRepository).save(any(Organization.class));
        verify(eventPublisher).publishOrganizationCreated(any(OrganizationDTO.class), eq(currentUserId));
    }

    @Test
    @DisplayName("Should throw exception when registry number already exists")
    void shouldThrowExceptionForDuplicateRegistry() {
        var request = new CreateOrganizationRequest(
            "Tech Corp", "REG12345", "contact@techcorp.com", 100, 2020
        );

        when(organizationRepository.existsByRegistryNumber("REG12345")).thenReturn(true);

        assertThatThrownBy(() -> organizationService.createOrganization(request, UUID.randomUUID()))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("already exists");

        verify(organizationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get organization by ID successfully")
    void shouldGetOrganizationById() {
        var orgId = UUID.randomUUID();
        var org = createMockOrganization();
        var expectedDTO = createMockOrgDTO();

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(organizationMapper.toDto(org)).thenReturn(expectedDTO);

        var result = organizationService.getOrganizationById(orgId);

        assertThat(result).isNotNull();
        assertThat(result.organizationName()).isEqualTo("Tech Corp");
    }

    @Test
    @DisplayName("Should throw exception when organization not found by ID")
    void shouldThrowExceptionWhenNotFoundById() {
        var orgId = UUID.randomUUID();

        when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizationService.getOrganizationById(orgId))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get organization by registry number")
    void shouldGetOrganizationByRegistry() {
        var org = createMockOrganization();
        var expectedDTO = createMockOrgDTO();

        when(organizationRepository.findByRegistryNumber("REG12345")).thenReturn(Optional.of(org));
        when(organizationMapper.toDto(org)).thenReturn(expectedDTO);

        var result = organizationService.getByRegistryNumber("REG12345");

        assertThat(result.registryNumber()).isEqualTo("REG12345");
    }

    @Test
    @DisplayName("Should soft-delete organization successfully")
    void shouldSoftDeleteOrganizationSuccessfully() {
        // Given
        var orgId = UUID.randomUUID();
        var currentUserId = UUID.randomUUID();
        var org = new Organization();
        org.setId(orgId);
        org.setStatus(OrganizationStatus.ACTIVE);

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));

        // When
        organizationService.deleteOrganization(orgId, currentUserId);

        // Then
        assertThat(org.getStatus()).isEqualTo(OrganizationStatus.DELETED);
        assertThat(org.getUpdatedBy()).isEqualTo(currentUserId);

        verify(organizationRepository).save(org);
    }

    @Test
    @DisplayName("Should throw exception when soft-deleting non-existent organization")
    void shouldThrowExceptionWhenSoftDeletingNonExistentOrganization() {
        // Given
        var orgId = UUID.randomUUID();
        when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> organizationService.deleteOrganization(orgId, UUID.randomUUID()))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining(orgId.toString());

        verify(organizationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get organization users")
    void shouldGetOrganizationUsers() {
        var orgId = UUID.randomUUID();
        var org = createMockOrganization();
        org.setUserIds(List.of(UUID.randomUUID(), UUID.randomUUID()));

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));

        var result = organizationService.getOrganizationUsers(orgId);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should add user to organization")
    void shouldAddUserToOrganization() {
        var orgId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var org = createMockOrganization();

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(i -> i.getArgument(0));

        organizationService.addUserToOrganization(orgId, userId);

        verify(organizationRepository).save(argThat(o -> o.getUserIds().contains(userId)));
    }

    private Organization createMockOrganization() {
        var org = new Organization();
        org.setId(UUID.randomUUID());
        org.setOrganizationName("Tech Corp");
        org.setNormalizedOrganizationName("tech corp");
        org.setRegistryNumber("REG12345");
        org.setContactEmail("contact@techcorp.com");
        org.setCompanySize(100);
        org.setYearFounded(2020);
        return org;
    }

    private OrganizationDTO createMockOrgDTO() {
        return new OrganizationDTO(
            UUID.randomUUID(),
            "Tech Corp",
            "tech corp",
            "REG12345",
            "contact@techcorp.com",
            100,
            2020,
            OrganizationStatus.ACTIVE,
            List.of()
        );
    }
}