package com.bloodbank.auth;

import com.bloodbank.auth.dto.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/register/donor")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthUserResponse registerDonor(@Valid @RequestBody RegisterDonorRequest r) {
        return auth.registerDonor(r);
    }

    @PostMapping("/register/hospital")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthUserResponse registerHospital(@Valid @RequestBody RegisterHospitalRequest r) {
        return auth.registerHospital(r);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest r) {
        return auth.login(r);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public AuthUserResponse me() { return auth.me(); }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest r) {
        auth.changePassword(r);
    }
}
