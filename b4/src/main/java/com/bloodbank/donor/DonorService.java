package com.bloodbank.donor;

import com.bloodbank.common.dto.PageResponse;
import com.bloodbank.common.exception.BusinessException;
import com.bloodbank.common.exception.NotFoundException;
import com.bloodbank.donor.dto.*;
import com.bloodbank.security.SecurityUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DonorService {

    private final DonorRepository donors;
    private final DonationQueryPort donationPort;
    private final DonorRules rules;

    public DonorService(DonorRepository donors, DonationQueryPort donationPort, DonorRules rules) {
        this.donors = donors;
        this.donationPort = donationPort;
        this.rules = rules;
    }

    // ---------- donor self-service ----------
    public DonorProfileResponse getMyProfile() {
        return toProfile(currentDonor());
    }

    @Transactional
    public DonorProfileResponse updateMyProfile(UpdateDonorRequest req) {
        Donor d = currentDonor();
        if (req.phone() != null) d.setPhone(req.phone().trim());
        if (req.weightKg() != null) d.setWeightKg(req.weightKg());
        if (req.city() != null) d.setCity(req.city().trim());
        if (req.bloodGroup() != null && !req.bloodGroup().equals(d.getBloodGroup())) {
            if (hasDonations(d.getId())) {
                throw new BusinessException("BLOOD_GROUP_LOCKED",
                        "Blood group cannot be changed after a donation exists");
            }
            d.setBloodGroup(req.bloodGroup());
        }
        return toProfile(donors.save(d));
    }

    public EligibilityResponse getMyEligibility() {
        return evaluate(currentDonor());
    }

    public List<DonationSummary> getMyDonations() {
        return donationPort.findByDonorId(currentDonor().getId());
    }

    // ---------- shared with other modules ----------
    public EligibilityResponse getEligibility(Long donorId) {
        return evaluate(find(donorId));
    }

    public boolean isEligible(Long donorId) {
        return evaluate(find(donorId)).eligible();
    }

    public boolean hasDonations(Long donorId) {
        return donationPort.hasDonations(donorId);
    }

    // ---------- staff / admin ----------
    public PageResponse<DonorProfileResponse> search(String q, String bloodGroup, String city, Pageable pageable) {
        String pattern = (q == null || q.isBlank()) ? null : "%" + q.trim() + "%";
        return PageResponse.from(donors.search(pattern, blankToNull(bloodGroup), blankToNull(city), pageable)
                .map(this::toProfile));
    }

    public DonorDetailResponse getDetail(Long id) {
        Donor d = find(id);
        return new DonorDetailResponse(toProfile(d), evaluate(d), donationPort.findByDonorId(id));
    }

    public PageResponse<DonorProfileResponse> listEligible(String bloodGroup, Pageable pageable) {
        LocalDate today = LocalDate.now();
        return PageResponse.from(donors.findEligible(
                bloodGroup, rules.getMinWeightKg(),
                today.minusYears(rules.getMinAge()),
                today.minusYears(rules.getMaxAge() + 1L),
                today.minusDays(rules.getDonationGapDays()),
                today, pageable).map(this::toProfile));
    }

    // ---------- internals ----------
    private EligibilityResponse evaluate(Donor d) {
        LocalDate today = LocalDate.now();
        LocalDate last = d.getLastDonationDate();
        LocalDate gapDate = last == null ? null : last.plusDays(rules.getDonationGapDays());

        if (!d.getUser().isActive())
            return no("ACCOUNT_INACTIVE", "Account is deactivated", last, null);
        if (d.getDeferredUntil() != null && !d.getDeferredUntil().isBefore(today)) {
            String why = d.getDeferralReason() == null ? "Temporarily deferred" : d.getDeferralReason();
            return no("DEFERRED", why, last, d.getDeferredUntil().plusDays(1));
        }
        if (d.getDateOfBirth() == null)
            return no("AGE", "Date of birth missing", last, null);
        int age = Period.between(d.getDateOfBirth(), today).getYears();
        if (age < rules.getMinAge() || age > rules.getMaxAge())
            return no("AGE", "Age must be between " + rules.getMinAge() + " and " + rules.getMaxAge(), last, null);
        if (d.getWeightKg() == null || d.getWeightKg() < rules.getMinWeightKg())
            return no("WEIGHT", "Weight must be at least " + rules.getMinWeightKg() + " kg", last, null);
        if (gapDate != null && today.isBefore(gapDate))
            return no("GAP", "Last donation was less than " + rules.getDonationGapDays() + " days ago", last, gapDate);
        return new EligibilityResponse(true, null, null, last, null);
    }

    private EligibilityResponse no(String code, String reason, LocalDate last, LocalDate next) {
        return new EligibilityResponse(false, code, reason, last, next);
    }

    private DonorProfileResponse toProfile(Donor d) {
        return new DonorProfileResponse(d.getId(), d.getFullName(), d.getUser().getEmail(), d.getPhone(),
                d.getDateOfBirth(), d.getWeightKg(), d.getCity(), d.getBloodGroup(),
                hasDonations(d.getId()), d.getLastDonationDate());
    }

    private Donor currentDonor() {
        return donors.findByUserId(SecurityUtils.currentUserId())
                .orElseThrow(() -> new NotFoundException("Donor profile not found"));
    }

    private Donor find(Long id) {
        return donors.findById(id).orElseThrow(() -> new NotFoundException("Donor not found"));
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
