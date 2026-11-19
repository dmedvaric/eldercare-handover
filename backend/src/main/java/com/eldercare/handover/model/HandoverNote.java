package com.eldercare.handover.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

/** Free-text message from one shift to the next. Stays visible until a nurse acknowledges it. */
@Entity
public class HandoverNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Resident resident;

    @Enumerated(EnumType.STRING)
    private Shift shift;

    @Column(length = 2000)
    private String content;

    private String author;
    private LocalDateTime createdAt;
    private String acknowledgedBy;
    private LocalDateTime acknowledgedAt;

    protected HandoverNote() {
    }

    public HandoverNote(Resident resident, Shift shift, String content, String author, LocalDateTime createdAt) {
        this.resident = resident;
        this.shift = shift;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
    }

    public void acknowledge(String nurse, LocalDateTime at) {
        this.acknowledgedBy = nurse;
        this.acknowledgedAt = at;
    }

    public boolean isAcknowledged() {
        return acknowledgedAt != null;
    }

    public Long getId() { return id; }
    public Resident getResident() { return resident; }
    public Shift getShift() { return shift; }
    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getAcknowledgedBy() { return acknowledgedBy; }
    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
}
