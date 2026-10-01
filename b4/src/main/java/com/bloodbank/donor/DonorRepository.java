package com.bloodbank.donor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface DonorRepository extends JpaRepository<Donor, Long> {

    Optional<Donor> findByUserId(Long userId);

    /** q is a pre-built "%text%" pattern or null. */
    @Query("""
        select d from Donor d
        where (:bloodGroup is null or d.bloodGroup = :bloodGroup)
          and (:city is null or lower(d.city) = lower(:city))
          and (:q is null or lower(d.fullName) like lower(:q) or d.phone like :q)
        """)
    Page<Donor> search(@Param("q") String q, @Param("bloodGroup") String bloodGroup,
                       @Param("city") String city, Pageable pageable);

    @Query("""
        select d from Donor d
        where d.bloodGroup = :bloodGroup
          and d.user.active = true
          and d.weightKg >= :minWeight
          and d.dateOfBirth <= :latestDob
          and d.dateOfBirth > :earliestDobExclusive
          and (d.lastDonationDate is null or d.lastDonationDate <= :lastDonationCutoff)
          and (d.deferredUntil is null or d.deferredUntil < :today)
        """)
    Page<Donor> findEligible(@Param("bloodGroup") String bloodGroup,
                             @Param("minWeight") double minWeight,
                             @Param("latestDob") LocalDate latestDob,
                             @Param("earliestDobExclusive") LocalDate earliestDobExclusive,
                             @Param("lastDonationCutoff") LocalDate lastDonationCutoff,
                             @Param("today") LocalDate today,
                             Pageable pageable);
}
