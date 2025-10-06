package com.digitopia.common.util;

import com.digitopia.common.enums.Role;
import com.digitopia.common.exception.UnauthorizedException;
import java.util.List;
import java.util.UUID;

public final class AuthorizationUtil {

    private AuthorizationUtil() {}

    /**
     * Checks if current user can access a specific user's record.
     * ADMIN: can access anyone
     * MANAGER: can access anyone
     * USER: can only access own record
     */
    public static void checkUserAccess(UUID targetUserId, UUID currentUserId, Role currentUserRole) {
        if (currentUserRole == Role.ADMIN || currentUserRole == Role.MANAGER) {
            return;
        }


        if (!targetUserId.equals(currentUserId)) {
            throw new UnauthorizedException("Users can only access their own records");
        }
    }

    /**
     * Checks if current user can perform delete operations.
     * Only ADMIN can delete.
     */
    public static void checkDeletePermission(Role currentUserRole) {
        if (currentUserRole != Role.ADMIN) {
            throw new UnauthorizedException("Only ADMIN can perform delete operations");
        }
    }

    /**
     * Checks if current user is member of the organization.
     * Used for invitation and organization management.
     */
    public static void checkOrganizationMembership(
        UUID organizationId,
        List<UUID> userOrganizationIds,
        Role currentUserRole
    ) {
        if (currentUserRole == Role.ADMIN) {
            return;
        }

        if (!userOrganizationIds.contains(organizationId)) {
            throw new UnauthorizedException(
                "You must be a member of this organization to perform this action"
            );
        }
    }

    /**
     * Parses role from header.
     */

    public static Role parseRole(String roleHeader) {
        if (roleHeader == null || roleHeader.isBlank()) {
            throw new UnauthorizedException("Missing role header");
        }

        try {
            var roleName = roleHeader.startsWith("ROLE_")
                ? roleHeader.substring(5)
                : roleHeader;
            return Role.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid role: " + roleHeader);
        }
    }

}
