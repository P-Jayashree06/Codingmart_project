package com.bloodbank.donor;

import com.bloodbank.donor.dto.DonationSummary;
import org.springframework.stereotype.Component;
import java.util.List;

/** STUB. B5 must DELETE this class when it adds the real DonationQueryPort bean. */
@Component
public class EmptyDonationQueryPort implements DonationQueryPort {
    @Override public List<DonationSummary> findByDonorId(Long donorId) { return List.of(); }
    @Override public boolean hasDonations(Long donorId) { return false; }
}
