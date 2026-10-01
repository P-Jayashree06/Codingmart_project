package com.bloodbank.user;

import com.bloodbank.auth.dto.AuthUserResponse;
import com.bloodbank.auth.dto.CreateStaffRequest;
import com.bloodbank.auth.dto.SetActiveRequest;
import com.bloodbank.common.PageResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin users")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final UserService users;

    public AdminUserController(UserService users) { this.users = users; }

    @PostMapping("/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthUserResponse createStaff(@Valid @RequestBody CreateStaffRequest r) {
        return users.createStaff(r);
    }

    @GetMapping("/users")
    public PageResponse<AuthUserResponse> list(@RequestParam(required = false) Role role,
                                               @RequestParam(required = false) Boolean active,
                                               @RequestParam(required = false) String q,
                                               @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return users.list(role, active, q, pageable);
    }

    @PatchMapping("/users/{id}/active")
    public AuthUserResponse setActive(@PathVariable Long id, @Valid @RequestBody SetActiveRequest r) {
        return users.setActive(id, r.active());
    }
}
