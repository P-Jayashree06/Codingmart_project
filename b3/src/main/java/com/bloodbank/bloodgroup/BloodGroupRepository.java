package com.bloodbank.bloodgroup;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BloodGroupRepository extends JpaRepository<BloodGroup, Long> {
    Optional<BloodGroup> findByCodeIgnoreCase(String code);
}
