package com.bloodbank.security;

import com.bloodbank.user.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static Long currentUserId() { return details().getId(); }
    public static Role currentRole() { return details().getRole(); }

    private static CustomUserDetails details() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !(a.getPrincipal() instanceof CustomUserDetails d)) {
            throw new IllegalStateException("No authenticated user in context");
        }
        return d;
    }
}
