package com.eldercare.handover.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String room;
    private String bed;

    @Column(length = 1000)
    private String careNotes;

    private LocalDateTime lastBedChange;
    private int bedChangeIntervalDays;

    protected Resident() {
    }

    public Resident(String fullName, String room, String bed, String careNotes,
                    LocalDateTime lastBedChange, int bedChangeIntervalDays) {
        this.fullName = fullName;
        this.room = room;
        this.bed = bed;
        this.careNotes = careNotes;
        this.lastBedChange = lastBedChange;
        this.bedChangeIntervalDays = bedChangeIntervalDays;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getRoom() { return room; }
    public String getBed() { return bed; }
    public String getCareNotes() { return careNotes; }
    public LocalDateTime getLastBedChange() { return lastBedChange; }
    public int getBedChangeIntervalDays() { return bedChangeIntervalDays; }

    public void setLastBedChange(LocalDateTime lastBedChange) { this.lastBedChange = lastBedChange; }
}
