package com.example.booking.security;

import com.example.booking.enums.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new AuthenticationCredentialsNotFoundException("User is not authenticated");
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public static String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }

    public static Role getCurrentUserRole() {
        return getCurrentUser().getRole();
    }

    public static boolean isCurrentUserAdmin() {
        return getCurrentUser().isAdmin();
    }

    public static void validateOwnershipOrAdmin(Long resourceOwnerId) {
        UserPrincipal currentUser = getCurrentUser();
        if (!currentUser.isAdmin() && !currentUser.getId().equals(resourceOwnerId)) {
            throw new AccessDeniedException("You do not have permission to access or modify this reservation");
        }
    }
}
