package com.eldercare.handover.api;

import com.eldercare.handover.model.AdministrationStatus;
import com.eldercare.handover.model.BedChangeStatus;
import com.eldercare.handover.model.Shift;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class Dtos {

    private Dtos() {
    }


    public record NurseRequest(String nurse) {
    }

    public record MoodRequest(int score, String note, String nurse) {
    }

    public record AdministerRequest(AdministrationStatus status, String reason, String nurse) {
    }

    public record MedicationRequest(String name, String dose, LocalTime scheduledTime, boolean onlyToday, String nurse) {
    }

    public record TaskRequest(String description, boolean onlyToday, String nurse) {
    }

    public record NoteRequest(Shift shift, String content, String nurse) {
    }

    // ---------- responses ----------

    public record MoodView(int score, String note, LocalDateTime recordedAt, String nurse) {
    }

    public record MoodDay(LocalDate date, double average) {
    }

    /** status == null means "not yet handled today". */
    public record MedicationView(Long id, String name, String dose, LocalTime scheduledTime, boolean onlyToday,
                                 AdministrationStatus status, String reason, String nurse,
                                 LocalDateTime recordedAt) {
    }

    public record TaskView(Long id, String description, boolean onlyToday, boolean done, String doneBy,
                           LocalDateTime doneAt) {
    }

    public record NoteView(Long id, Shift shift, String content, String author, LocalDateTime createdAt,
                           String acknowledgedBy, LocalDateTime acknowledgedAt) {
    }

    public record ResidentOverview(Long id, String fullName, String room, String bed, String careNotes,
                                   LocalDateTime lastBedChange, LocalDate bedChangeDueDate, BedChangeStatus bedStatus,
                                   MoodView todayMood, List<MoodDay> moodHistory, boolean moodDeclining,
                                   List<MedicationView> medications, List<TaskView> tasks, List<NoteView> notes,
                                   int pendingMedications, int openTasks, int unacknowledgedNotes) {
    }

    public record ActivityView(Long id, LocalDateTime occurredAt, Long residentId, String residentName,
                               String eventType, String summary, String nurse) {
    }
}
