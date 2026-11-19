package com.eldercare.handover.seed;

import com.eldercare.handover.model.ActivityEvent;
import com.eldercare.handover.model.AdministrationStatus;
import com.eldercare.handover.model.CareTask;
import com.eldercare.handover.model.HandoverNote;
import com.eldercare.handover.model.Medication;
import com.eldercare.handover.model.MedicationAdministration;
import com.eldercare.handover.model.MoodEntry;
import com.eldercare.handover.model.Resident;
import com.eldercare.handover.model.Shift;
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
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// some mock data
@Component
public class DataSeeder implements CommandLineRunner {

    private static final String EVENING_NURSE = "Nurse Petra";
    private static final String MORNING_NURSE = "Nurse Tomas";

    private final ResidentRepository residents;
    private final MoodEntryRepository moods;
    private final MedicationRepository medications;
    private final MedicationAdministrationRepository administrations;
    private final CareTaskRepository tasks;
    private final HandoverNoteRepository notes;
    private final ActivityEventRepository activityEvents;
    private final Clock clock;

    private LocalDateTime now;
    private LocalDate today;

    public DataSeeder(ResidentRepository residents, MoodEntryRepository moods, MedicationRepository medications,
                      MedicationAdministrationRepository administrations, CareTaskRepository tasks,
                      HandoverNoteRepository notes, ActivityEventRepository activityEvents, Clock clock) {
        this.residents = residents;
        this.moods = moods;
        this.medications = medications;
        this.administrations = administrations;
        this.tasks = tasks;
        this.notes = notes;
        this.activityEvents = activityEvents;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (residents.count() > 0) {
            return;
        }
        now = LocalDateTime.now(clock);
        today = now.toLocalDate();

        // ---- Anna: stable and cheerful
        Resident anna = resident("Anna Berger", "101", "A",
                "Hard of hearing: speak slowly, face her. Walks with a rollator.", now.minusDays(3), 7);
        moodHistory(anna, 4, 4, 5, 4, 4, 4);
        mood(anna, 4, "Chatty at breakfast", todayAt(8, 15), MORNING_NURSE);
        medication(anna, "Ramipril", "5 mg", 8, 0, false, AdministrationStatus.GIVEN, null);
        medication(anna, "Vitamin D", "1000 IU", 8, 0, false, AdministrationStatus.GIVEN, null);
        task(anna, "Encourage fluids, aim for 1.5 litres today", false, false);
        task(anna, "Check hearing aid battery", true, false);

        // ---- Franz: bed change overdue, refused a morning dose
        Resident franz = resident("Franz Huber", "101", "B",
                "Diabetic diet. Needs help cutting food at meals.", now.minusDays(8), 7);
        moodHistory(franz, 3, 3, 4, 3, 3, 3);
        medication(franz, "Metformin", "500 mg", 8, 0, false, AdministrationStatus.NOT_GIVEN,
                "Refused, felt nauseous after breakfast");
        medication(franz, "Metformin", "500 mg", 18, 0, false, null, null);
        medication(franz, "Atorvastatin", "20 mg", 20, 0, false, null, null);
        task(franz, "Weigh Franz (doctor request)", true, false);
        task(franz, "Offer a walk in the garden", false, false);
        note(franz, Shift.LATE, EVENING_NURSE, now.minusHours(10), null,
                "Refused the evening walk and mentioned back pain. Doctor will visit today. Please ask how his back is.");

        // ---- Maria: mood has been declining for days
        Resident maria = resident("Maria Gruber", "102", "A",
                "Prefers tea over coffee. Daughter visits on Sundays.", now.minusDays(7), 7);
        moodHistory(maria, 4, 4, 3, 3, 2, 2);
        medication(maria, "Levothyroxine", "50 µg", 6, 30, false, AdministrationStatus.GIVEN, null);
        medication(maria, "Paracetamol", "500 mg", 14, 0, true, null, null);
        medication(maria, "Calcium", "500 mg", 18, 0, false, null, null);
        task(maria, "Sit with Maria for 10 minutes and offer her favourite tea", false, false);
        note(maria, Shift.LATE, EVENING_NURSE, now.minusHours(10), null,
                "Ate only half of her dinner and was very quiet. Please keep an eye on appetite and mood.");

        // ---- Josef: fall risk, bed freshly changed
        Resident josef = resident("Josef Wagner", "103", "A",
                "Fall risk. Bed alarm must be on at night.", now.minusHours(20), 3);
        moodHistory(josef, 4, 4, 4, 5, 4, 4);
        mood(josef, 5, null, todayAt(9, 0), MORNING_NURSE);
        medication(josef, "Amlodipine", "5 mg", 8, 0, false, AdministrationStatus.GIVEN, null);
        medication(josef, "Melatonin", "2 mg", 21, 30, false, null, null);
        task(josef, "Switch the bed alarm on before lights out", false, false);

        // ---- Helga: restless night
        Resident helga = resident("Helga Pichler", "104", "A",
                "Dementia: reorient gently, never argue. Likes music in the afternoon.", now.minusDays(5), 7);
        moodHistory(helga, 3, 3, 3, 3, 2, 3);
        medication(helga, "Lactulose", "15 ml", 8, 0, false, AdministrationStatus.GIVEN, null);
        medication(helga, "Donepezil", "5 mg", 20, 0, false, null, null);
        task(helga, "Accompany Helga to the day room at 10:00", true, false);
        note(helga, Shift.NIGHT, EVENING_NURSE, now.minusHours(5), null,
                "Restless around 03:00, calmed down with tea and a short talk. Slept until 06:00.");

        // ---- Karl: wound care
        Resident karl = resident("Karl Steiner", "105", "B",
                "Dressing on the left heel, check daily. Uses a wheelchair for longer distances.", now.minusDays(2), 7);
        moodHistory(karl, 3, 4, 3, 3, 3, 4);
        medication(karl, "Cefalexin", "500 mg", 12, 0, false, null, null);
        medication(karl, "Cefalexin", "500 mg", 20, 0, false, null, null);
        task(karl, "Change heel dressing and note wound condition", true, false);
        task(karl, "Keep the left heel off-loaded when he is in bed", false, false);
        note(karl, Shift.EARLY, MORNING_NURSE, now.minusHours(4), now.minusHours(2),
                "Wound looks cleaner than yesterday, no redness. Dressing was changed at 07:30.");
    }

    // ------------------------------------------------------------------ helpers

    private LocalDateTime todayAt(int hour, int minute) {
        LocalDateTime t = today.atTime(hour, minute);
        return t.isAfter(now) ? now.minusMinutes(1) : t;
    }

    private Resident resident(String name, String room, String bed, String careNotes,
                              LocalDateTime lastBedChange, int intervalDays) {
        return residents.save(new Resident(name, room, bed, careNotes, lastBedChange, intervalDays));
    }

    /** Scores for the previous days, oldest first (the last value is yesterday). */
    private void moodHistory(Resident resident, int... scoresOldestFirst) {
        int days = scoresOldestFirst.length;
        for (int i = 0; i < days; i++) {
            long daysAgo = days - i;
            moods.save(new MoodEntry(resident, scoresOldestFirst[i], null,
                    today.minusDays(daysAgo).atTime(10, 0), EVENING_NURSE));
        }
    }

    private void mood(Resident resident, int score, String note, LocalDateTime at, String nurse) {
        moods.save(new MoodEntry(resident, score, note, at, nurse));
        event(resident, at, "MOOD", "Mood recorded: " + score + " of 5", nurse);
    }

    /** A recurring medication, or one that is only valid today. The outcome is only stored if the time has passed. */
    private void medication(Resident resident, String name, String dose, int hour, int minute,
                            boolean todayOnly, AdministrationStatus outcome, String reason) {
        Medication medication = medications.save(
                new Medication(resident, name, dose, LocalTime.of(hour, minute), todayOnly ? today : null));
        LocalDateTime due = today.atTime(hour, minute);
        if (todayOnly) {
            event(resident, now.minusHours(3), "MEDICATION_ADDED",
                    "New medication for today only: " + name + " " + dose + " (doctor's order)", MORNING_NURSE);
        }
        if (outcome != null && !todayOnly && due.isBefore(now)) {
            LocalDateTime at = due.plusMinutes(5).isAfter(now) ? now : due.plusMinutes(5);
            MedicationAdministration administration = new MedicationAdministration(medication, today);
            administration.update(outcome, reason, MORNING_NURSE, at);
            administrations.save(administration);
            event(resident, at,
                    outcome == AdministrationStatus.GIVEN ? "MEDICATION_GIVEN" : "MEDICATION_NOT_GIVEN",
                    outcome == AdministrationStatus.GIVEN
                            ? name + " " + dose + " given"
                            : name + " " + dose + " NOT given: " + reason,
                    MORNING_NURSE);
        }
    }

    private void task(Resident resident, String description, boolean todayOnly, boolean done) {
        CareTask task = new CareTask(resident, description, todayOnly ? today : null, EVENING_NURSE,
                now.minusHours(9));
        if (done) {
            task.complete(MORNING_NURSE, now.minusHours(1));
        }
        tasks.save(task);
    }

    private void note(Resident resident, Shift shift, String author, LocalDateTime createdAt,
                      LocalDateTime acknowledgedAt, String content) {
        HandoverNote note = new HandoverNote(resident, shift, content, author, createdAt);
        if (acknowledgedAt != null) {
            note.acknowledge(MORNING_NURSE, acknowledgedAt);
        }
        notes.save(note);
        event(resident, createdAt, "NOTE", "Handover note: " + content, author);
    }

    private void event(Resident resident, LocalDateTime at, String type, String summary, String nurse) {
        activityEvents.save(new ActivityEvent(at, resident.getId(), resident.getFullName(), type, summary, nurse));
    }
}
