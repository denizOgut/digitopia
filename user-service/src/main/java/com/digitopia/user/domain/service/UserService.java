package com.digitopia.user.domain.service;

import com.digitopia.common.dto.UserDTO;
import com.digitopia.common.dto.request.CreateUserRequest;
import com.digitopia.common.dto.request.SearchUserRequest;
import com.digitopia.common.dto.request.UpdateUserStatusRequest;
import com.digitopia.common.enums.Role;
import com.digitopia.common.enums.UserStatus;
import com.digitopia.common.exception.DuplicateResourceException;
import com.digitopia.common.exception.ResourceNotFoundException;
import com.digitopia.common.exception.ValidationException;
import com.digitopia.common.util.StringUtils;
import com.digitopia.user.domain.entity.User;
import com.digitopia.user.domain.repository.UserRepository;
import com.digitopia.user.infrastructure.mapper.UserMapper;
import com.digitopia.user.infrastructure.messaging.EventConsumer;
import com.digitopia.user.infrastructure.messaging.UserEventPublisher;

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
 * Service for managing user operations.
 *
 * <p>This service handles complete user lifecycle management including creation,
 * retrieval, updates, deletion, and search operations. It integrates with Redis
 * for caching frequently accessed user data and publishes events for inter-service
 * communication.</p>
 *
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserEventPublisher eventPublisher;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(
        UserRepository userRepository,
        UserMapper userMapper,
        UserEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new user.
     *
     * @param request user creation request
     * @param currentUserId ID of user creating this user
     * @param roleFromAuth role of the authenticated user (from JWT)
     * @return created user DTO
     * @throws DuplicateResourceException if email already exists
     * @throws ValidationException if full name contains non-letter characters
     */
    @Transactional
    public UserDTO createUser(CreateUserRequest request, UUID currentUserId, Role roleFromAuth) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User already exists with email: " + request.email());
        }

        if (!request.fullName().matches("^[a-zA-Z\\s]+$")) {
            throw new ValidationException("Full name must contain only letters");
        }

        var user = new User();
        user.setEmail(StringUtils.normalizeEmail(request.email()));
        user.setFullName(StringUtils.sanitize(request.fullName()));
        user.setNormalizedName(StringUtils.normalizeToAscii(request.fullName()));
        user.setRole(request.role() != null ? request.role() : Role.USER);


        user.setStatus(roleFromAuth == Role.ADMIN ? UserStatus.ACTIVE : UserStatus.PENDING);

        user.setCreatedBy(currentUserId);
        user.setUpdatedBy(currentUserId);

        var saved = userRepository.save(user);
        var dto = userMapper.toDto(saved);

        eventPublisher.publishUserCreated(dto, currentUserId);

        return dto;
    }

    /**
     * Retrieves a user by ID.
     * Result is cached in Redis.
     *
     * @param id user ID
     * @return user DTO
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userById", key = "#id")
    public UserDTO getUserById(UUID id) {
        var user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User " + id.toString()));
        return userMapper.toDto(user);
    }

    /**
     * Retrieves a user by email address.
     * Result is cached in Redis.
     *
     * @param email user email
     * @return user DTO
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userByEmail", key = "#email")
    public UserDTO getUserByEmail(String email) {
        var user = userRepository.findByEmail(StringUtils.normalizeEmail(email))
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toDto(user);
    }

    /**
     * Searches users by normalized name.
     * Supports partial matching and pagination.
     *
     * @param request search criteria (name, page, size)
     * @return paginated user results
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> searchByName(SearchUserRequest request) {
        var pageable = PageRequest.of(request.page(), request.size());
        var normalizedName = StringUtils.normalizeToAscii(request.normalizedName());

        return userRepository.findByNormalizedNameContaining(normalizedName, pageable)
            .map(userMapper::toDto);
    }

    /**
     * Updates user status.
     * Evicts user from cache.
     *
     * @param id user ID
     * @param request new status
     * @param currentUserId ID of user making the update
     * @return updated user DTO
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    @CacheEvict(value = {"userById", "userByEmail"}, allEntries = true)
    public UserDTO updateStatus(UUID id, UpdateUserStatusRequest request, UUID currentUserId) {
        var user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User " + id.toString()));

        user.setStatus(request.status());
        user.setUpdatedBy(currentUserId);

        var saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    /**
     * Gets list of organization IDs the user belongs to.
     *
     * @param userId user ID
     * @return list of organization UUIDs
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public List<UUID> getUserOrganizations(UUID userId) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User " + userId.toString()));
        return user.getOrganizationIds();
    }

    /**
     * Adds user to an organization.
     * Called by invitation-service when invitation is accepted.
     *
     * @param userId user ID
     * @param organizationId organization ID to add
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public void addUserToOrganization(UUID userId, UUID organizationId) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User " + userId.toString()));

        if (!user.getOrganizationIds().contains(organizationId)) {
            user.getOrganizationIds().add(organizationId);
            userRepository.save(user);
        }
    }

    /**
     * Soft-deletes a user by setting status to DELETED.
     * User record remains in database but is marked as deleted.
     *
     * @param id user ID
     * @param currentUserId ID of user performing the deletion
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    @CacheEvict(value = {"userById", "userByEmail"}, key = "#id")
    public void deleteUser(UUID id, UUID currentUserId) {
        var user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User " + id.toString()));

        user.setStatus(UserStatus.DELETED);
        user.setUpdatedBy(currentUserId);
        userRepository.save(user);

        log.info("User {} soft-deleted by user {}", id, currentUserId);
    }
}
