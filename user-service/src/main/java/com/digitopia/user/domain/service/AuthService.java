package com.digitopia.user.domain.service;

import com.digitopia.common.constants.AppConstants;
import com.digitopia.common.dto.UserDTO;
import com.digitopia.common.dto.request.CreateUserRequest;
import com.digitopia.common.enums.Role;
import com.digitopia.common.enums.UserStatus;
import com.digitopia.common.exception.DuplicateResourceException;
import com.digitopia.common.exception.ValidationException;
import com.digitopia.common.util.StringUtils;
import com.digitopia.user.domain.entity.User;
import com.digitopia.user.domain.repository.UserRepository;
import com.digitopia.user.infrastructure.mapper.UserMapper;
import com.digitopia.user.infrastructure.messaging.UserEventPublisher;
import com.digitopia.user.infrastructure.security.JwtTokenProvider;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


/**
 * Service for authentication operations including user registration and login.
 *
 * <p>This service handles secure authentication workflows by managing password
 * hashing with BCrypt and generating JWT tokens for stateless authentication.
 * It enforces security best practices and validates user credentials before
 * granting access.</p>
 *
 * <p>Key Responsibilities:</p>
 * <ul>
 *   <li>User registration with automatic password hashing</li>
 *   <li>User authentication with credential validation</li>
 *   <li>JWT token generation for authenticated sessions</li>
 *   <li>Account status verification (active, deleted, deactivated)</li>
 *   <li>Email uniqueness enforcement during registration</li>
 *   <li>Input validation and sanitization</li>
 * </ul>
 *
 *
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserEventPublisher eventPublisher;

    public AuthService(
        UserRepository userRepository,
        UserMapper userMapper,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider jwtTokenProvider,
        UserEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Registers a new user in the system.
     * Creates user with PENDING status and USER role.
     *
     * @param request user registration data
     * @param password plain text password (will be hashed)
     * @return map containing JWT token and user DTO
     * @throws DuplicateResourceException if email already exists
     * @throws ValidationException if full name contains non-letters
     */
    @Transactional
    public Map<String, Object> register(CreateUserRequest request, String password) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User already exists with email: " + request.email());
        }

        if (!request.fullName().matches("^[a-zA-Z\\s]+$")) {
            throw new ValidationException("Full name must contain only letters");
        }

        var user = new User();
        user.setEmail(StringUtils.normalizeEmail(request.email()));
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(StringUtils.sanitize(request.fullName()));
        user.setNormalizedName(StringUtils.normalizeToAscii(request.fullName()));
        user.setRole(Role.USER);
        user.setStatus(UserStatus.PENDING);

        user.setCreatedBy(AppConstants.SYSTEM_USER_ID);
        user.setUpdatedBy(AppConstants.SYSTEM_USER_ID);

        var saved = userRepository.save(user);
        var dto = userMapper.toDto(saved);

        var token = jwtTokenProvider.generateToken(saved.getId(), saved.getEmail(), saved.getRole());

        eventPublisher.publishUserCreated(dto, AppConstants.SYSTEM_USER_ID);

        return Map.of(
            "token", token,
            "user", dto
        );
    }

    /**
     * Authenticates a user and returns JWT token.
     *
     * @param email user's email
     * @param password plain text password
     * @return map containing JWT token and user DTO
     * @throws ValidationException if credentials invalid or account not active
     */
    @Transactional(readOnly = true)
    public Map<String, Object> login(String email, String password) {
        var user = userRepository.findByEmail(StringUtils.normalizeEmail(email))
            .orElseThrow(() -> new ValidationException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ValidationException("Invalid email or password");
        }

        if (user.getStatus() == UserStatus.DELETED || user.getStatus() == UserStatus.DEACTIVATED) {
            throw new ValidationException("Account is not active");
        }

        var token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
        var dto = userMapper.toDto(user);

        return Map.of(
            "token", token,
            "user", dto
        );
    }
}

