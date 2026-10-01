package com.bloodbank.donor;

import com.bloodbank.common.dto.PageResponse;
import com.bloodbank.donor.dto.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donors")
@SecurityRequirement(name = "bearerAuth")
public class DonorController {

    private final DonorService service;

    public DonorController(DonorService service) { this.service = service; }

    // ----- donor self -----
    @GetMapping("/me")
    @PreAuthorize("hasRole('DONOR')")
    public DonorProfileResponse me() { return service.getMyProfile(); }

    @PutMapping("/me")
    @PreAuthorize("hasRole('DONOR')")
    public DonorProfileResponse updateMe(@Valid @RequestBody UpdateDonorRequest req) {
        return service.updateMyProfile(req);
    }

    @GetMapping("/me/eligibility")
    @PreAuthorize("hasRole('DONOR')")
    public EligibilityResponse myEligibility() { return service.getMyEligibility(); }

    @GetMapping("/me/donations")
    @PreAuthorize("hasRole('DONOR')")
    public List<DonationSummary> myDonations() { return service.getMyDonations(); }

    // ----- staff / admin -----
    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public PageResponse<DonorProfileResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) String city,
            @PageableDefault(size = 20, sort = "fullName") Pageable pageable) {
        return service.search(q, bloodGroup, city, pageable);
    }

    @GetMapping("/eligible")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public PageResponse<DonorProfileResponse> eligible(
            @RequestParam String bloodGroup,
            @PageableDefault(size = 20, sort = "fullName") Pageable pageable) {
        return service.listEligible(bloodGroup, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public DonorDetailResponse detail(@PathVariable Long id) { return service.getDetail(id); }
}
