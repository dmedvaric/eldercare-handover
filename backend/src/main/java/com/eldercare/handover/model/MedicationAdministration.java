package com.eldercare.handover.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Whether a medication was given (or not, and why) on a particular day. At most one per medication and day. TODO might be changed */
@Entity
public class MedicationAdministration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Medication medication;

    private LocalDate administeredOn;

    @Enumerated(EnumType.STRING)
    private AdministrationStatus status;

    @Column(length = 500)
    private String reason;

    private String nurse;
    private LocalDateTime recordedAt;

    protected MedicationAdministration() {
    }

    public MedicationAdministration(Medication medication, LocalDate administeredOn) {
        this.medication = medication;
        this.administeredOn = administeredOn;
    }

    public void update(AdministrationStatus status, String reason, String nurse, LocalDateTime recordedAt) {
        this.status = status;
        this.reason = reason;
        this.nurse = nurse;
        this.recordedAt = recordedAt;
    }

    public Long getId() { return id; }
    public Medication getMedication() { return medication; }
    public LocalDate getAdministeredOn() { return administeredOn; }
    public AdministrationStatus getStatus() { return status; }
    public String getReason() { return reason; }
    public String getNurse() { return nurse; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
}
