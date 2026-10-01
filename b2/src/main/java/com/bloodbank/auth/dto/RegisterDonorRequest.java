package com.bloodbank.auth.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record RegisterDonorRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 100) @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "must contain at least one letter and one digit")
        String password,
        @NotBlank @Size(max = 150) String fullName,
        @Size(max = 20) String phone,
        @NotNull @Past LocalDate dateOfBirth,
        @Size(max = 20) String gender,
        @NotNull Long bloodGroupId) {}
