package com.bloodbank.donor.dto;

import jakarta.validation.constraints.*;

public record UpdateDonorRequest(
        @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$", message = "invalid phone") String phone,
        @DecimalMin(value = "30.0", message = "weight too low") @DecimalMax(value = "300.0") Double weightKg,
        @Size(max = 100) String city,
        @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "invalid blood group") String bloodGroup) {}
