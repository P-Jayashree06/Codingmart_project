package com.bloodbank.bloodgroup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompatibilityRepository extends JpaRepository<BloodCompatibility, BloodCompatibilityId> {

    /** Donor group ids usable by the given recipient group, ordered by id. */
    @Query("""
        select c.id.donorGroupId from BloodCompatibility c
        where c.id.recipientGroupId = :recipientId
        order by c.id.donorGroupId
        """)
    List<Long> findDonorGroupIdsByRecipient(@Param("recipientId") Long recipientId);
}
