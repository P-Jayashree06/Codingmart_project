package com.bloodbank.user;

import com.bloodbank.auth.dto.AuthUserResponse;
import com.bloodbank.auth.dto.CreateStaffRequest;
import com.bloodbank.common.PageResponse;
import com.bloodbank.exception.BusinessException;
import com.bloodbank.exception.ConflictException;
import com.bloodbank.exception.ResourceNotFound;
import com.bloodbank.security.SecurityUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public AuthUserResponse createStaff(CreateStaffRequest r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw ConflictException.emailExists(email);
        }
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(r.password()));
        u.setFullName(r.fullName().trim());
        u.setPhone(r.phone());
        u.setRole(Role.STAFF);
        u.setActive(true);
        return AuthUserResponse.from(users.save(u));
    }

    @Transactional(readOnly = true)
    public PageResponse<AuthUserResponse> list(Role role, Boolean active, String q, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        return PageResponse.from(users.search(role, active, query, pageable), AuthUserResponse::from);
    }

    @Transactional
    public AuthUserResponse setActive(Long id, boolean active) {
        if (!active && id.equals(SecurityUtils.currentUserId())) {
            throw new BusinessException("CANNOT_DEACTIVATE_SELF", HttpStatus.BAD_REQUEST,
                    "Admins cannot deactivate their own account");
        }
        User u = users.findById(id).orElseThrow(() -> new ResourceNotFound("User", id));
        u.setActive(active);
        return AuthUserResponse.from(users.save(u));
    }
}
