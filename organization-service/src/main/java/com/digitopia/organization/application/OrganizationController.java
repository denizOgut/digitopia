package com.digitopia.organization.application;

import com.digitopia.common.constants.AppConstants;
import com.digitopia.common.dto.OrganizationDTO;
import com.digitopia.common.dto.request.CreateOrganizationRequest;
import com.digitopia.common.dto.request.SearchOrganizationRequest;
import com.digitopia.common.enums.Role;
import com.digitopia.common.exception.UnauthorizedException;
import com.digitopia.common.util.AuthorizationUtil;
import com.digitopia.organization.domain.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
@Tag(name = "Organization Management", description = "Organization CRUD operations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    @Operation(summary = "Create new organization")
    public ResponseEntity<OrganizationDTO> createOrganization(
        @Valid @RequestBody CreateOrganizationRequest request,
        @RequestHeader(AppConstants.HEADER_USER_ID) UUID currentUserId
    ) {
        var org = organizationService.createOrganization(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(org);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID")
    public ResponseEntity<OrganizationDTO> getOrganizationById(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.getOrganizationById(id));
    }

    @GetMapping("/registry/{registryNumber}")
    @Operation(summary = "Search organization by registry number")
    public ResponseEntity<OrganizationDTO> getByRegistryNumber(@PathVariable String registryNumber) {
        return ResponseEntity.ok(organizationService.getByRegistryNumber(registryNumber));
    }

    @GetMapping("/search")
    @Operation(summary = "Search organizations by name, year, size")
    public ResponseEntity<Page<OrganizationDTO>> searchOrganizations(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer size,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        var request = new SearchOrganizationRequest(name, year, size, null, page, pageSize);
        return ResponseEntity.ok(organizationService.searchOrganizations(request));
    }

    @GetMapping("/{id}/users")
    @Operation(summary = "Get organization's users")
    public ResponseEntity<List<UUID>> getOrganizationUsers(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.getOrganizationUsers(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete organization (soft delete)")
    public ResponseEntity<Void> deleteOrganization(
        @PathVariable UUID id,
        @RequestHeader(AppConstants.HEADER_USER_ID) UUID currentUserId,
        @RequestHeader(AppConstants.HEADER_USER_ROLE) String roleHeader
    ) {
        var role = AuthorizationUtil.parseRole(roleHeader);
        AuthorizationUtil.checkDeletePermission(role);

        organizationService.deleteOrganization(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
