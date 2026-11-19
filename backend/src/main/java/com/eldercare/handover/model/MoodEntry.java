package com.eldercare.handover.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class MoodEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Resident resident;

    private int score;

    @Column(length = 500)
    private String note;

    private LocalDateTime recordedAt;
    private String nurse;

    protected MoodEntry() {
    }

    public MoodEntry(Resident resident, int score, String note, LocalDateTime recordedAt, String nurse) {
        this.resident = resident;
        this.score = score;
        this.note = note;
        this.recordedAt = recordedAt;
        this.nurse = nurse;
    }

    public Long getId() { return id; }
    public Resident getResident() { return resident; }
    public int getScore() { return score; }
    public String getNote() { return note; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public String getNurse() { return nurse; }
}
