package com.bloodbank.auth.dto;

import jakarta.validation.constraints.*;

public record RegisterHospitalRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 100) @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "must contain at least one letter and one digit")
        String password,
        @NotBlank @Size(max = 200) String hospitalName,
        @NotBlank @Size(max = 100) String licenseNo,
        @Size(max = 500) String address,
        @Size(max = 20) String phone) {}
