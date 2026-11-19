package com.eldercare.handover.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

/**
 * This is a simple feed for the incoming shift, not a legal audit log (that comes later).
 */
@Entity
public class ActivityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime occurredAt;
    private Long residentId;
    private String residentName;
    private String eventType;

    @Column(length = 600)
    private String summary;

    private String nurse;

    protected ActivityEvent() {
    }

    public ActivityEvent(LocalDateTime occurredAt, Long residentId, String residentName,
                         String eventType, String summary, String nurse) {
        this.occurredAt = occurredAt;
        this.residentId = residentId;
        this.residentName = residentName;
        this.eventType = eventType;
        this.summary = summary;
        this.nurse = nurse;
    }

    public Long getId() { return id; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public Long getResidentId() { return residentId; }
    public String getResidentName() { return residentName; }
    public String getEventType() { return eventType; }
    public String getSummary() { return summary; }
    public String getNurse() { return nurse; }
}
