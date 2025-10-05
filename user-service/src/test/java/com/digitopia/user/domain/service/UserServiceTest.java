package com.digitopia.user.domain.service;

import com.digitopia.common.dto.UserDTO;
import com.digitopia.common.dto.request.CreateUserRequest;
import com.digitopia.common.dto.request.UpdateUserStatusRequest;
import com.digitopia.common.enums.Role;
import com.digitopia.common.enums.UserStatus;
import com.digitopia.common.exception.DuplicateResourceException;
import com.digitopia.common.exception.ResourceNotFoundException;
import com.digitopia.user.domain.entity.User;
import com.digitopia.user.domain.repository.UserRepository;
import com.digitopia.user.infrastructure.mapper.UserMapper;
import com.digitopia.user.infrastructure.messaging.UserEventPublisher;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserEventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;


    @Test
    @DisplayName("Should create user with PENDING status when creator is not ADMIN")
    void shouldCreatePendingUserForNonAdmin() {
        var request = new CreateUserRequest("test@example.com", "John Doe", Role.USER);
        var currentUserId = UUID.randomUUID();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(userMapper.toDto(any(User.class))).thenReturn(createUserDTO(UserStatus.PENDING));

        var result = userService.createUser(request, currentUserId, Role.USER);

        assertThat(result.status()).isEqualTo(UserStatus.PENDING);
        verify(userRepository).save(argThat(user -> user.getStatus() == UserStatus.PENDING));
        verify(eventPublisher).publishUserCreated(any(UserDTO.class), eq(currentUserId));
    }

    @Test
    @DisplayName("Should create user with ACTIVE status when creator is ADMIN")
    void shouldCreateActiveUserForAdmin() {
        var request = new CreateUserRequest("admin@example.com", "Admin User", Role.ADMIN);
        var adminId = UUID.randomUUID();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(userMapper.toDto(any(User.class))).thenReturn(createUserDTO(UserStatus.ACTIVE));

        var result = userService.createUser(request, adminId, Role.ADMIN);

        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository).save(argThat(user -> user.getStatus() == UserStatus.ACTIVE));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionForDuplicateEmail() {
        var request = new CreateUserRequest("existing@example.com", "Jane Doe", Role.USER);

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request, UUID.randomUUID(), Role.USER))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
    }


    @Test
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserById() {
        var userId = UUID.randomUUID();
        var user = createUser();
        var expectedDTO = createUserDTO(UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(expectedDTO);

        var result = userService.getUserById(userId);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void shouldThrowExceptionWhenUserNotFoundById() {
        var userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get user by email successfully")
    void shouldGetUserByEmail() {
        var user = createUser();
        var expectedDTO = createUserDTO(UserStatus.ACTIVE);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(expectedDTO);

        var result = userService.getUserByEmail("test@example.com");

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should update user status successfully")
    void shouldUpdateStatus() {
        var userId = UUID.randomUUID();
        var user = createUser();
        var request = new UpdateUserStatusRequest(UserStatus.ACTIVE);
        var currentUserId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(userMapper.toDto(any(User.class))).thenReturn(createUserDTO(UserStatus.ACTIVE));

        var result = userService.updateStatus(userId, request, currentUserId);

        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository).save(argThat(u -> u.getStatus() == UserStatus.ACTIVE));
    }

    @Test
    @DisplayName("Should soft delete user by setting status to DELETED")
    void shouldSoftDeleteUser() {
        var userId = UUID.randomUUID();
        var user = createUser();
        var currentUserId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.deleteUser(userId, currentUserId);

        verify(userRepository).save(argThat(u -> u.getStatus() == UserStatus.DELETED));
    }

    @Test
    @DisplayName("Should add user to organization")
    void shouldAddUserToOrganization() {
        var userId = UUID.randomUUID();
        var orgId = UUID.randomUUID();
        var user = createUser();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.addUserToOrganization(userId, orgId);

        verify(userRepository).save(argThat(u -> u.getOrganizationIds().contains(orgId)));
    }

    private User createUser() {
        var user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setFullName("John Doe");
        user.setNormalizedName("john doe");
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(Role.USER);
        return user;
    }

    private UserDTO createUserDTO(UserStatus status) {
        return new UserDTO(
            UUID.randomUUID(),
            "test@example.com",
            status,
            "John Doe",
            "john doe",
            Role.USER,
            List.of()
        );
    }
}
