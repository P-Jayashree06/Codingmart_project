package com.bloodbank.bloodgroup;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Reference data (seeded in V2). Fixed ids: 1=A+ 2=A- 3=B+ 4=B- 5=AB+ 6=AB- 7=O+ 8=O-. */
@Entity
@Immutable
@Table(name = "blood_group")
public class BloodGroup {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 5)
    private String code;

    protected BloodGroup() {}

    public BloodGroup(Long id, String code) {
        this.id = id;
        this.code = code;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
}
