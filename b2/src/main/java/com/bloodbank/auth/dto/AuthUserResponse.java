package com.bloodbank.auth.dto;

import com.bloodbank.user.Role;
import com.bloodbank.user.User;

public record AuthUserResponse(Long id, String email, String fullName, Role role, boolean active) {
    public static AuthUserResponse from(User u) {
        return new AuthUserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRole(), u.isActive());
    }
}
