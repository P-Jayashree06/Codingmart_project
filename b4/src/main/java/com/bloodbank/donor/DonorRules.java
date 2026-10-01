package com.bloodbank.donor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Binds bloodbank.donor.* from application.yml. */
@Component
@ConfigurationProperties(prefix = "bloodbank.donor")
public class DonorRules {
    private int minAge = 18;
    private int maxAge = 65;
    private double minWeightKg = 50;
    private int donationGapDays = 90;

    public int getMinAge() { return minAge; }
    public void setMinAge(int v) { this.minAge = v; }
    public int getMaxAge() { return maxAge; }
    public void setMaxAge(int v) { this.maxAge = v; }
    public double getMinWeightKg() { return minWeightKg; }
    public void setMinWeightKg(double v) { this.minWeightKg = v; }
    public int getDonationGapDays() { return donationGapDays; }
    public void setDonationGapDays(int v) { this.donationGapDays = v; }
}
