package com.learnloop.security;

import com.learnloop.common.exceptions.UnauthorizedException;
import com.learnloop.domain.user.entity.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtils {

    public static Optional<UserPrincipal> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static UserPrincipal getRequiredCurrentUser() {
        return getCurrentUser().orElseThrow(() -> new UnauthorizedException("User is not authenticated"));
    }

    public static String getCurrentUserId() {
        return getRequiredCurrentUser().getId();
    }

    public static UserRole getCurrentUserRole() {
        return getRequiredCurrentUser().getRole();
    }

    public static boolean isCurrentUser(String userId) {
        return getCurrentUser()
                .map(user -> user.getId().equals(userId))
                .orElse(false);
    }

    public static boolean hasRole(UserRole role) {
        return getCurrentUser()
                .map(user -> user.getRole() == role)
                .orElse(false);
    }
}
