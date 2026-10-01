package com.bloodbank.donor.dto;

import java.time.LocalDate;

public record DonorProfileResponse(
        Long id, String fullName, String email, String phone, LocalDate dateOfBirth,
        Double weightKg, String city, String bloodGroup, boolean bloodGroupLocked,
        LocalDate lastDonationDate) {}
