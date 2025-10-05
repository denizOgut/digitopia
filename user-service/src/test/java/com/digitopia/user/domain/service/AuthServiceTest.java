package com.digitopia.user.domain.service;

import com.digitopia.common.dto.UserDTO;
import com.digitopia.common.dto.request.CreateUserRequest;
import com.digitopia.common.enums.Role;
import com.digitopia.common.enums.UserStatus;
import com.digitopia.common.exception.DuplicateResourceException;
import com.digitopia.common.exception.ValidationException;
import com.digitopia.user.domain.entity.User;
import com.digitopia.user.domain.repository.UserRepository;
import com.digitopia.user.infrastructure.mapper.UserMapper;
import com.digitopia.user.infrastructure.messaging.UserEventPublisher;
import com.digitopia.user.infrastructure.security.JwtTokenProvider;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserEventPublisher eventPublisher;

    @InjectMocks
    private AuthService authService;


    @Test
    @DisplayName("Should register new user successfully")
    void shouldRegisterSuccessfully() {
        var request = new CreateUserRequest("newuser@example.com", "John Doe", Role.USER);
        var mockUserDTO = new UserDTO(
            UUID.randomUUID(), "newuser@example.com", UserStatus.PENDING,
            "John Doe", "john doe", Role.USER, List.of()
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(jwtTokenProvider.generateToken(any(), anyString(), any())).thenReturn("jwt-token");
        when(userMapper.toDto(any())).thenReturn(mockUserDTO);

        var result = authService.register(request, "password123");

        assertThat(result).containsKeys("token", "user");
        assertThat(result.get("token")).isEqualTo("jwt-token");
        assertThat(result.get("user")).isEqualTo(mockUserDTO);
    }

    @Test
    @DisplayName("Should throw exception when registering with existing email")
    void shouldThrowExceptionForDuplicateEmailOnRegister() {
        var request = new CreateUserRequest("existing@example.com", "John Doe", Role.USER);

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request, "password"))
            .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }


    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() {
        var user = createActiveUser();

        var mockUserDTO = new UserDTO(
            UUID.randomUUID(), "newuser@example.com", UserStatus.PENDING,
            "John Doe", "john doe", Role.USER, List.of()
        );

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(), anyString(), any())).thenReturn("jwt-token");
        when(userMapper.toDto(user)).thenReturn(mockUserDTO);

        var result = authService.login("test@example.com", "password123");

        assertThat(result).containsKeys("token", "user");
        assertThat(result.get("token")).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("Should throw exception for invalid password")
    void shouldThrowExceptionForInvalidPassword() {
        var user = createActiveUser();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login("test@example.com", "wrongpassword"))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("Should throw exception when user is deleted")
    void shouldThrowExceptionForDeletedUser() {
        var user = createActiveUser();
        user.setStatus(UserStatus.DELETED);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> authService.login("test@example.com", "password123"))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("not active");
    }


    private User createActiveUser() {
        var user = new User();
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(Role.USER);
        return user;
    }
}
