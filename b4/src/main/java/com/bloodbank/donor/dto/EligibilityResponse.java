package com.bloodbank.donor.dto;

import java.time.LocalDate;

/** reasonCode: ACCOUNT_INACTIVE, DEFERRED, AGE, WEIGHT, GAP (null when eligible). */
public record EligibilityResponse(
        boolean eligible, String reasonCode, String reason,
        LocalDate lastDonationDate, LocalDate nextEligibleDate) {}
