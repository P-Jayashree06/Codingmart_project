package com.bloodbank.bloodgroup;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** Row means: a recipient of group recipient_group_id can receive blood from donor_group_id. */
@Entity
@Immutable
@Table(name = "blood_compatibility")
public class BloodCompatibility {

    @EmbeddedId
    private BloodCompatibilityId id;

    protected BloodCompatibility() {}

    public BloodCompatibility(BloodCompatibilityId id) { this.id = id; }

    public BloodCompatibilityId getId() { return id; }
}
