package com.eldercare.handover.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A medication order. If onlyOnDate is null it is given every day7 otherwise it applies
 * to that single date only (e.g. a one-off dose ordered by the doctor) and disappears afterwards.
 */
@Entity
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Resident resident;

    private String name;
    private String dose;
    private LocalTime scheduledTime;
    private LocalDate onlyOnDate;

    protected Medication() {
    }

    public Medication(Resident resident, String name, String dose, LocalTime scheduledTime, LocalDate onlyOnDate) {
        this.resident = resident;
        this.name = name;
        this.dose = dose;
        this.scheduledTime = scheduledTime;
        this.onlyOnDate = onlyOnDate;
    }

    public boolean appliesOn(LocalDate date) {
        return onlyOnDate == null || onlyOnDate.equals(date);
    }

    public Long getId() { return id; }
    public Resident getResident() { return resident; }
    public String getName() { return name; }
    public String getDose() { return dose; }
    public LocalTime getScheduledTime() { return scheduledTime; }
    public LocalDate getOnlyOnDate() { return onlyOnDate; }
}
