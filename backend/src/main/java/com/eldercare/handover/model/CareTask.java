package com.eldercare.handover.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Something the next shift has to do. If onlyOnDate is set the task is "today only" and
 * automatically disappears after that date/ otherwise it stays until somebody completes it.
 */
@Entity
public class CareTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Resident resident;

    @Column(length = 500)
    private String description;

    private LocalDate onlyOnDate;
    private boolean done;
    private String doneBy;
    private LocalDateTime doneAt;
    private String createdBy;
    private LocalDateTime createdAt;

    protected CareTask() {
    }

    public CareTask(Resident resident, String description, LocalDate onlyOnDate,
                    String createdBy, LocalDateTime createdAt) {
        this.resident = resident;
        this.description = description;
        this.onlyOnDate = onlyOnDate;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public void complete(String nurse, LocalDateTime at) {
        this.done = true;
        this.doneBy = nurse;
        this.doneAt = at;
    }

    public Long getId() { return id; }
    public Resident getResident() { return resident; }
    public String getDescription() { return description; }
    public LocalDate getOnlyOnDate() { return onlyOnDate; }
    public boolean isDone() { return done; }
    public String getDoneBy() { return doneBy; }
    public LocalDateTime getDoneAt() { return doneAt; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
