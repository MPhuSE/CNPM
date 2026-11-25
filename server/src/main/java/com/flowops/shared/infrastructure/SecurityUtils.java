package com.flowops.shared.infrastructure;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<Long> getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            if (auth.getPrincipal() instanceof SessionPrincipal principal) return Optional.of(principal.userId());
            if (auth.getPrincipal() instanceof Long userId) return Optional.of(userId);
        }
        return Optional.empty();
    }

    public static SessionPrincipal getRequiredSessionPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof SessionPrincipal principal) return principal;
        throw new org.springframework.security.authentication.InsufficientAuthenticationException("Phiên không hợp lệ");
    }

    public static Long getRequiredCurrentUserId() {
        return getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy thông tin người dùng trong phiên làm việc"));
    }
}
