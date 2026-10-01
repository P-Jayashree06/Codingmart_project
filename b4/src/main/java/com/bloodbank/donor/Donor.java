package com.bloodbank.donor;

import com.bloodbank.user.User;
import jakarta.persistence.*;
import java.time.LocalDate;

/** Full donor profile (extends the B2 stub). */
@Entity
@Table(name = "donor")
public class Donor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;
    private String phone;
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    @Column(name = "weight_kg")
    private Double weightKg;
    private String city;
    @Column(name = "blood_group", nullable = false, length = 3)
    private String bloodGroup;
    @Column(name = "last_donation_date")
    private LocalDate lastDonationDate;
    @Column(name = "deferred_until")
    private LocalDate deferredUntil;
    @Column(name = "deferral_reason")
    private String deferralReason;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public LocalDate getLastDonationDate() { return lastDonationDate; }
    public void setLastDonationDate(LocalDate d) { this.lastDonationDate = d; }
    public LocalDate getDeferredUntil() { return deferredUntil; }
    public void setDeferredUntil(LocalDate d) { this.deferredUntil = d; }
    public String getDeferralReason() { return deferralReason; }
    public void setDeferralReason(String r) { this.deferralReason = r; }
}
