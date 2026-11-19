package com.eldercare.handover.service;

import com.eldercare.handover.api.Dtos.ActivityView;
import com.eldercare.handover.api.Dtos.AdministerRequest;
import com.eldercare.handover.api.Dtos.MedicationRequest;
import com.eldercare.handover.api.Dtos.MedicationView;
import com.eldercare.handover.api.Dtos.MoodDay;
import com.eldercare.handover.api.Dtos.MoodRequest;
import com.eldercare.handover.api.Dtos.MoodView;
import com.eldercare.handover.api.Dtos.NoteRequest;
import com.eldercare.handover.api.Dtos.NoteView;
import com.eldercare.handover.api.Dtos.ResidentOverview;
import com.eldercare.handover.api.Dtos.TaskRequest;
import com.eldercare.handover.api.Dtos.TaskView;
import com.eldercare.handover.model.ActivityEvent;
import com.eldercare.handover.model.AdministrationStatus;
import com.eldercare.handover.model.BedChangeStatus;
import com.eldercare.handover.model.CareTask;
import com.eldercare.handover.model.HandoverNote;
import com.eldercare.handover.model.Medication;
import com.eldercare.handover.model.MedicationAdministration;
import com.eldercare.handover.model.MoodEntry;
import com.eldercare.handover.model.Resident;
import com.eldercare.handover.repo.ActivityEventRepository;
import com.eldercare.handover.repo.CareTaskRepository;
import com.eldercare.handover.repo.HandoverNoteRepository;
import com.eldercare.handover.repo.MedicationAdministrationRepository;
import com.eldercare.handover.repo.MedicationRepository;
import com.eldercare.handover.repo.MoodEntryRepository;
import com.eldercare.handover.repo.ResidentRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * All business rules of the handover board live here. Every write method returns the refreshed
 * overview of the affected resident, so the UI can simply replace that resident's card.
 */
@Service
public class HandoverService {

    private static final int MOOD_WINDOW_DAYS = 7;
    private static final int NOTE_WINDOW_HOURS = 36;

    private final ResidentRepository residents;
    private final MoodEntryRepository moods;
    private final MedicationRepository medications;
    private final MedicationAdministrationRepository administrations;
    private final CareTaskRepository tasks;
    private final HandoverNoteRepository notes;
    private final ActivityEventRepository activityEvents;
    private final Clock clock;

    public HandoverService(ResidentRepository residents,
                           MoodEntryRepository moods,
                           MedicationRepository medications,
                           MedicationAdministrationRepository administrations,
                           CareTaskRepository tasks,
                           HandoverNoteRepository notes,
                           ActivityEventRepository activityEvents,
                           Clock clock) {
        this.residents = residents;
        this.moods = moods;
        this.medications = medications;
        this.administrations = administrations;
        this.tasks = tasks;
        this.notes = notes;
        this.activityEvents = activityEvents;
        this.clock = clock;
    }

    // ------------------------------------------------------------------ reads

    @Transactional(readOnly = true)
    public List<ResidentOverview> overview() {
        return buildOverviews();
    }

    @Transactional(readOnly = true)
    public List<ActivityView> activity(int hours) {
        int window = Math.max(1, Math.min(hours, 72));
        LocalDateTime since = LocalDateTime.now(clock).minusHours(window);
        return activityEvents.findByOccurredAtGreaterThanEqualOrderByOccurredAtDesc(since).stream()
                .map(e -> new ActivityView(e.getId(), e.getOccurredAt(), e.getResidentId(), e.getResidentName(),
                        e.getEventType(), e.getSummary(), e.getNurse()))
                .toList();
    }

    // ----------------------------------------------------------------- writes

    @Transactional
    public ResidentOverview changeBed(Long residentId, String nurseName) {
        Resident resident = findResident(residentId);
        String nurse = requireNurse(nurseName);
        resident.setLastBedChange(LocalDateTime.now(clock));
        log(resident, "BED_CHANGE", "Bed linen changed", nurse);
        return overviewOf(residentId);
    }

    @Transactional
    public ResidentOverview recordMood(Long residentId, MoodRequest request) {
        Resident resident = findResident(residentId);
        String nurse = requireNurse(request.nurse());
        if (request.score() < 1 || request.score() > 5) {
            throw bad("Mood score must be between 1 and 5");
        }
        moods.save(new MoodEntry(resident, request.score(), blankToNull(request.note()),
                LocalDateTime.now(clock), nurse));
        log(resident, "MOOD", "Mood recorded: " + request.score() + " of 5", nurse);
        return overviewOf(residentId);
    }

    @Transactional
    public ResidentOverview addMedication(Long residentId, MedicationRequest request) {
        Resident resident = findResident(residentId);
        String nurse = requireNurse(request.nurse());
        String name = requireText(request.name(), "Medication name");
        LocalDateTime now = LocalDateTime.now(clock);
        LocalTime time = request.scheduledTime() != null
                ? request.scheduledTime()
                : now.toLocalTime().truncatedTo(ChronoUnit.MINUTES);
        LocalDate onlyOn = request.onlyToday() ? now.toLocalDate() : null;
        medications.save(new Medication(resident, name, blankToNull(request.dose()), time, onlyOn));
        log(resident, "MEDICATION_ADDED",
                (request.onlyToday() ? "New medication for today only: " : "New daily medication: ")
                        + name + (blankToNull(request.dose()) != null ? " " + request.dose().trim() : ""),
                nurse);
        return overviewOf(residentId);
    }

    @Transactional
    public ResidentOverview administer(Long medicationId, AdministerRequest request) {
        Medication medication = medications.findById(medicationId)
                .orElseThrow(() -> notFound("Medication"));
        String nurse = requireNurse(request.nurse());
        if (request.status() == null) {
            throw bad("Status is required");
        }
        String reason = blankToNull(request.reason());
        if (request.status() == AdministrationStatus.NOT_GIVEN && reason == null) {
            throw bad("Please give a reason when a medication was not given");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate today = now.toLocalDate();
        if (!medication.appliesOn(today)) {
            throw bad("This medication is not scheduled for today");
        }

        MedicationAdministration administration = administrations
                .findByMedicationIdAndAdministeredOn(medicationId, today)
                .orElseGet(() -> new MedicationAdministration(medication, today));
        boolean given = request.status() == AdministrationStatus.GIVEN;
        administration.update(request.status(), given ? null : reason, nurse, now);
        administrations.save(administration);

        String what = medication.getName() + (medication.getDose() != null ? " " + medication.getDose() : "");
        log(medication.getResident(), given ? "MEDICATION_GIVEN" : "MEDICATION_NOT_GIVEN",
                given ? what + " given" : what + " NOT given: " + reason, nurse);
        return overviewOf(medication.getResident().getId());
    }

    @Transactional
    public ResidentOverview addTask(Long residentId, TaskRequest request) {
        Resident resident = findResident(residentId);
        String nurse = requireNurse(request.nurse());
        String description = requireText(request.description(), "Task description");
        LocalDateTime now = LocalDateTime.now(clock);
        tasks.save(new CareTask(resident, description, request.onlyToday() ? now.toLocalDate() : null, nurse, now));
        log(resident, "TASK_ADDED",
                (request.onlyToday() ? "New task for today: " : "New task: ") + description, nurse);
        return overviewOf(residentId);
    }

    @Transactional
    public ResidentOverview completeTask(Long taskId, String nurseName) {
        CareTask task = tasks.findById(taskId).orElseThrow(() -> notFound("Task"));
        String nurse = requireNurse(nurseName);
        if (!task.isDone()) {
            task.complete(nurse, LocalDateTime.now(clock));
            log(task.getResident(), "TASK_DONE", "Done: " + task.getDescription(), nurse);
        }
        return overviewOf(task.getResident().getId());
    }

    @Transactional
    public ResidentOverview addNote(Long residentId, NoteRequest request) {
        Resident resident = findResident(residentId);
        String nurse = requireNurse(request.nurse());
        if (request.shift() == null) {
            throw bad("Shift is required");
        }
        String content = requireText(request.content(), "Note");
        notes.save(new HandoverNote(resident, request.shift(), content, nurse, LocalDateTime.now(clock)));
        log(resident, "NOTE", "Handover note: " + content, nurse);
        return overviewOf(residentId);
    }

    @Transactional
    public ResidentOverview acknowledgeNote(Long noteId, String nurseName) {
        HandoverNote note = notes.findById(noteId).orElseThrow(() -> notFound("Note"));
        String nurse = requireNurse(nurseName);
        if (!note.isAcknowledged()) {
            note.acknowledge(nurse, LocalDateTime.now(clock));
            log(note.getResident(), "NOTE_ACK", "Handover note read and acknowledged", nurse);
        }
        return overviewOf(note.getResident().getId());
    }

    // ------------------------------------------------------- building overviews

    private ResidentOverview overviewOf(Long residentId) {
        // Queries below auto-flush pending changes of the current transaction first.
        return buildOverviews().stream()
                .filter(o -> o.id().equals(residentId))
                .findFirst()
                .orElseThrow(() -> notFound("Resident"));
    }

    private List<ResidentOverview> buildOverviews() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate today = now.toLocalDate();

        Map<Long, List<MoodEntry>> moodsByResident = moods
                .findByRecordedAtGreaterThanEqualOrderByRecordedAtAsc(
                        today.minusDays(MOOD_WINDOW_DAYS - 1).atStartOfDay())
                .stream().collect(Collectors.groupingBy(m -> m.getResident().getId()));

        Map<Long, List<Medication>> medsByResident = medications.findActiveOn(today)
                .stream().collect(Collectors.groupingBy(m -> m.getResident().getId()));

        Map<Long, MedicationAdministration> administrationByMedication = administrations
                .findByAdministeredOn(today)
                .stream().collect(Collectors.toMap(a -> a.getMedication().getId(), Function.identity(), (a, b) -> b));

        Map<Long, List<CareTask>> tasksByResident = tasks.findVisible(today, today.atStartOfDay())
                .stream().collect(Collectors.groupingBy(t -> t.getResident().getId()));

        Map<Long, List<HandoverNote>> notesByResident = notes.findVisible(now.minusHours(NOTE_WINDOW_HOURS))
                .stream().collect(Collectors.groupingBy(n -> n.getResident().getId()));

        return residents.findAllByOrderByRoomAscBedAsc().stream()
                .map(r -> toOverview(r, now,
                        moodsByResident.getOrDefault(r.getId(), List.of()),
                        medsByResident.getOrDefault(r.getId(), List.of()),
                        administrationByMedication,
                        tasksByResident.getOrDefault(r.getId(), List.of()),
                        notesByResident.getOrDefault(r.getId(), List.of())))
                .toList();
    }

    private ResidentOverview toOverview(Resident resident, LocalDateTime now,
                                        List<MoodEntry> residentMoods,
                                        List<Medication> residentMeds,
                                        Map<Long, MedicationAdministration> administrationByMedication,
                                        List<CareTask> residentTasks,
                                        List<HandoverNote> residentNotes) {
        LocalDate today = now.toLocalDate();

        // bed linen
        LocalDate dueDate = resident.getLastBedChange().toLocalDate().plusDays(resident.getBedChangeIntervalDays());
        BedChangeStatus bedStatus = dueDate.isBefore(today) ? BedChangeStatus.OVERDUE
                : dueDate.isEqual(today) ? BedChangeStatus.DUE_TODAY
                : BedChangeStatus.OK;

        // mood: latest entry of today + 7 day history of daily averages
        MoodView todayMood = residentMoods.stream()
                .filter(m -> m.getRecordedAt().toLocalDate().equals(today))
                .reduce((first, second) -> second)
                .map(m -> new MoodView(m.getScore(), m.getNote(), m.getRecordedAt(), m.getNurse()))
                .orElse(null);
        List<MoodDay> history = moodHistory(residentMoods);

        // medication
        List<MedicationView> medicationViews = residentMeds.stream().map(m -> {
            MedicationAdministration a = administrationByMedication.get(m.getId());
            return new MedicationView(m.getId(), m.getName(), m.getDose(), m.getScheduledTime(),
                    m.getOnlyOnDate() != null,
                    a == null ? null : a.getStatus(),
                    a == null ? null : a.getReason(),
                    a == null ? null : a.getNurse(),
                    a == null ? null : a.getRecordedAt());
        }).toList();

        // tasks: open ones first
        List<TaskView> taskViews = residentTasks.stream()
                .sorted(Comparator.comparing(CareTask::isDone).thenComparing(CareTask::getId))
                .map(t -> new TaskView(t.getId(), t.getDescription(), t.getOnlyOnDate() != null,
                        t.isDone(), t.getDoneBy(), t.getDoneAt()))
                .toList();

        // notes: newest first (already ordered by the query)
        List<NoteView> noteViews = residentNotes.stream()
                .map(n -> new NoteView(n.getId(), n.getShift(), n.getContent(), n.getAuthor(), n.getCreatedAt(),
                        n.getAcknowledgedBy(), n.getAcknowledgedAt()))
                .toList();

        int pendingMedications = (int) medicationViews.stream().filter(m -> m.status() == null).count();
        int openTasks = (int) taskViews.stream().filter(t -> !t.done()).count();
        int unacknowledgedNotes = (int) noteViews.stream().filter(n -> n.acknowledgedAt() == null).count();

        return new ResidentOverview(resident.getId(), resident.getFullName(), resident.getRoom(), resident.getBed(),
                resident.getCareNotes(), resident.getLastBedChange(), dueDate, bedStatus,
                todayMood, history, isDeclining(history),
                medicationViews, taskViews, noteViews,
                pendingMedications, openTasks, unacknowledgedNotes);
    }

    private static List<MoodDay> moodHistory(List<MoodEntry> entries) {
        Map<LocalDate, Double> averageByDay = entries.stream().collect(Collectors.groupingBy(
                e -> e.getRecordedAt().toLocalDate(), TreeMap::new, Collectors.averagingInt(MoodEntry::getScore)));
        return averageByDay.entrySet().stream()
                .map(e -> new MoodDay(e.getKey(), Math.round(e.getValue() * 10.0) / 10.0))
                .toList();
    }

    /** Flags a resident whose last two recorded days are at least one point lower than the days before. */
    private static boolean isDeclining(List<MoodDay> history) {
        if (history.size() < 4) {
            return false;
        }
        int n = history.size();
        double recent = (history.get(n - 1).average() + history.get(n - 2).average()) / 2.0;
        double earlier = history.subList(0, n - 2).stream().mapToDouble(MoodDay::average).average().orElse(recent);
        return recent <= earlier - 1.0;
    }

    // ----------------------------------------------------------------- helpers

    private void log(Resident resident, String type, String summary, String nurse) {
        activityEvents.save(new ActivityEvent(LocalDateTime.now(clock), resident.getId(),
                resident.getFullName(), type, summary, nurse));
    }

    private Resident findResident(Long id) {
        return residents.findById(id).orElseThrow(() -> notFound("Resident"));
    }

    private static String requireNurse(String nurse) {
        if (nurse == null || nurse.isBlank()) {
            throw bad("Please enter your name at the top first");
        }
        return nurse.trim();
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw bad(field + " must not be empty");
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static ResponseStatusException notFound(String what) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " not found");
    }
}
