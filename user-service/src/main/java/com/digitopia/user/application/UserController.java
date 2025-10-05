package com.digitopia.user.application;

import com.digitopia.common.constants.AppConstants;
import com.digitopia.common.dto.UserDTO;
import com.digitopia.common.dto.request.CreateUserRequest;
import com.digitopia.common.dto.request.SearchUserRequest;
import com.digitopia.common.dto.request.UpdateUserStatusRequest;
import com.digitopia.common.enums.Role;
import com.digitopia.user.domain.service.UserService;
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
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "User CRUD operations")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Create new user")
    public ResponseEntity<UserDTO> createUser(
        @Valid @RequestBody CreateUserRequest request,
        @RequestHeader(AppConstants.HEADER_USER_ID) UUID currentUserId,
        @RequestHeader(AppConstants.HEADER_USER_ROLE) String roleHeader
    ) {
        var role = Role.valueOf(roleHeader.replace("ROLE_", ""));
        var user = userService.createUser(request, currentUserId, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Search user by email")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by normalized name")
    public ResponseEntity<Page<UserDTO>> searchUsers(
        @RequestParam String name,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var request = new SearchUserRequest(name, null, page, size);
        return ResponseEntity.ok(userService.searchByName(request));
    }

    @GetMapping("/{id}/organizations")
    @Operation(summary = "Get user's organizations")
    public ResponseEntity<List<UUID>> getUserOrganizations(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserOrganizations(id));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update user status")
    public ResponseEntity<UserDTO> updateStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateUserStatusRequest request,
        @RequestHeader(AppConstants.HEADER_USER_ID) UUID currentUserId
    ) {
        return ResponseEntity.ok(userService.updateStatus(id, request, currentUserId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user (soft delete)")
    public ResponseEntity<Void> deleteUser(
        @PathVariable UUID id,
        @RequestHeader(AppConstants.HEADER_USER_ID) UUID currentUserId
    ) {
        userService.deleteUser(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
