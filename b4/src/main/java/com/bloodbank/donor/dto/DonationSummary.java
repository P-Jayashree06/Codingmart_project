package com.bloodbank.donor.dto;

import java.time.LocalDate;

public record DonationSummary(
        Long donationId, LocalDate donationDate, Integer volumeMl,
        String screeningStatus, String unitNumber) {}
