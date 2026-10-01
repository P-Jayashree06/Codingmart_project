package com.bloodbank.bloodgroup;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BloodCompatibilityId implements Serializable {

    @Column(name = "recipient_group_id", nullable = false)
    private Long recipientGroupId;

    @Column(name = "donor_group_id", nullable = false)
    private Long donorGroupId;

    protected BloodCompatibilityId() {}

    public BloodCompatibilityId(Long recipientGroupId, Long donorGroupId) {
        this.recipientGroupId = recipientGroupId;
        this.donorGroupId = donorGroupId;
    }

    public Long getRecipientGroupId() { return recipientGroupId; }
    public Long getDonorGroupId() { return donorGroupId; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BloodCompatibilityId other)) return false;
        return Objects.equals(recipientGroupId, other.recipientGroupId)
                && Objects.equals(donorGroupId, other.donorGroupId);
    }

    @Override public int hashCode() { return Objects.hash(recipientGroupId, donorGroupId); }
}
