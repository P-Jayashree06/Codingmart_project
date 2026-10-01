package com.bloodbank.donor;

import com.bloodbank.donor.dto.DonationSummary;
import java.util.List;

/** Implemented by the donation module (B5). */
public interface DonationQueryPort {
    List<DonationSummary> findByDonorId(Long donorId);
    boolean hasDonations(Long donorId);
}
