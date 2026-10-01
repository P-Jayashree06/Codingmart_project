package com.bloodbank.auth;

import com.bloodbank.auth.dto.*;
import com.bloodbank.donor.Donor;
import com.bloodbank.donor.DonorRepository;
import com.bloodbank.exception.BusinessException;
import com.bloodbank.exception.ConflictException;
import com.bloodbank.exception.ResourceNotFound;
import com.bloodbank.hospital.ApprovalStatus;
import com.bloodbank.hospital.Hospital;
import com.bloodbank.hospital.HospitalRepository;
import com.bloodbank.security.JwtService;
import com.bloodbank.security.SecurityUtils;
import com.bloodbank.user.Role;
import com.bloodbank.user.User;
import com.bloodbank.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final DonorRepository donors;
    private final HospitalRepository hospitals;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, DonorRepository donors, HospitalRepository hospitals,
                       PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.donors = donors;
        this.hospitals = hospitals;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    /** User + donor profile are created in the same transaction. */
    @Transactional
    public AuthUserResponse registerDonor(RegisterDonorRequest r) {
        User u = newUser(r.email(), r.password(), r.fullName(), r.phone(), Role.DONOR);
        Donor d = new Donor();
        d.setUserId(u.getId());
        d.setBloodGroupId(r.bloodGroupId());
        d.setFullName(r.fullName().trim());
        d.setDateOfBirth(r.dateOfBirth());
        d.setGender(r.gender());
        d.setPhone(r.phone());
        donors.save(d);
        return AuthUserResponse.from(u);
    }

    /** User + hospital profile (approval_status=PENDING) are created in the same transaction. */
    @Transactional
    public AuthUserResponse registerHospital(RegisterHospitalRequest r) {
        if (hospitals.existsByLicenseNoIgnoreCase(r.licenseNo().trim())) {
            throw ConflictException.licenseExists(r.licenseNo());
        }
        User u = newUser(r.email(), r.password(), r.hospitalName(), r.phone(), Role.HOSPITAL);
        Hospital h = new Hospital();
        h.setUserId(u.getId());
        h.setName(r.hospitalName().trim());
        h.setLicenseNo(r.licenseNo().trim());
        h.setAddress(r.address());
        h.setPhone(r.phone());
        h.setApprovalStatus(ApprovalStatus.PENDING);
        hospitals.save(h);
        return AuthUserResponse.from(u);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest r) {
        User u = users.findByEmailIgnoreCase(r.email().trim())
                .orElseThrow(AuthService::invalidCredentials);
        if (!encoder.matches(r.password(), u.getPasswordHash())) {
            throw invalidCredentials();
        }
        if (!u.isActive()) {
            throw new BusinessException("ACCOUNT_DISABLED", HttpStatus.FORBIDDEN, "This account has been disabled");
        }
        String token = jwt.generateToken(u.getId(), u.getRole());
        return new LoginResponse(token, "Bearer", jwt.getExpiresInSeconds(), AuthUserResponse.from(u));
    }

    @Transactional(readOnly = true)
    public AuthUserResponse me() {
        return AuthUserResponse.from(currentUser());
    }

    @Transactional
    public void changePassword(ChangePasswordRequest r) {
        User u = currentUser();
        if (!encoder.matches(r.currentPassword(), u.getPasswordHash())) {
            throw new BusinessException("INVALID_CURRENT_PASSWORD", HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        u.setPasswordHash(encoder.encode(r.newPassword()));
        users.save(u);
    }

    private User newUser(String email, String rawPassword, String fullName, String phone, Role role) {
        String normalized = email.trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(normalized)) {
            throw ConflictException.emailExists(normalized);
        }
        User u = new User();
        u.setEmail(normalized);
        u.setPasswordHash(encoder.encode(rawPassword));
        u.setFullName(fullName.trim());
        u.setPhone(phone);
        u.setRole(role);
        u.setActive(true);
        return users.saveAndFlush(u);
    }

    private User currentUser() {
        Long id = SecurityUtils.currentUserId();
        return users.findById(id).orElseThrow(() -> new ResourceNotFound("User", id));
    }

    private static BusinessException invalidCredentials() {
        return new BusinessException("INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
}
