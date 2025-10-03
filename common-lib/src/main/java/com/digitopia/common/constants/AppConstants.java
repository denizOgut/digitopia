package com.digitopia.common.constants;

public final class AppConstants {
    private AppConstants() {}

    // RabbitMQ Exchanges
    public static final String USER_EXCHANGE = "digitopia.user.exchange";
    public static final String ORG_EXCHANGE = "digitopia.organization.exchange";
    public static final String INVITATION_EXCHANGE = "digitopia.invitation.exchange";

    // RabbitMQ Routing Keys
    public static final String USER_CREATED = "user.created";
    public static final String USER_UPDATED = "user.updated";
    public static final String USER_DELETED = "user.deleted";
    public static final String ORG_CREATED = "organization.created";
    public static final String ORG_UPDATED = "organization.updated";
    public static final String INVITATION_CREATED = "invitation.created";
    public static final String INVITATION_ACCEPTED = "invitation.accepted";
    public static final String INVITATION_REJECTED = "invitation.rejected";
    public static final String INVITATION_EXPIRED = "invitation.expired";

    // Cache Names
    public static final String CACHE_USER_EMAIL = "userByEmail";
    public static final String CACHE_USER_ID = "userById";
    public static final String CACHE_ORG_REGISTRY = "orgByRegistry";
    public static final String CACHE_ORG_ID = "orgById";
    public static final String CACHE_USER_ORGS = "userOrganizations";
    public static final String CACHE_ORG_USERS = "organizationUsers";

    // Cache TTL (seconds)
    public static final long CACHE_TTL_15MIN = 900;
    public static final long CACHE_TTL_5MIN = 300;

    // JWT
    public static final String JWT_SECRET = "digitopia-secret-key";
    public static final long JWT_EXPIRATION = 86400000; // 24 hours
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";

    // Headers
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";
    public static final String HEADER_USER_EMAIL = "X-User-Email";

    // Validation
    public static final int INVITATION_EXPIRY_DAYS = 7;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
}
