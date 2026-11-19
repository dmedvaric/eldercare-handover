package com.eldercare.handover;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.eldercare.handover.api.Dtos.AdministerRequest;
import com.eldercare.handover.api.Dtos.MedicationRequest;
import com.eldercare.handover.api.Dtos.MedicationView;
import com.eldercare.handover.api.Dtos.MoodRequest;
import com.eldercare.handover.api.Dtos.NoteRequest;
import com.eldercare.handover.api.Dtos.ResidentOverview;
import com.eldercare.handover.api.Dtos.TaskRequest;
import com.eldercare.handover.model.AdministrationStatus;
import com.eldercare.handover.model.BedChangeStatus;
import com.eldercare.handover.model.Shift;
import com.eldercare.handover.service.HandoverService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
class HandoverServiceTest {

    @Autowired
    HandoverService service;

    private ResidentOverview byName(String name) {
        return service.overview().stream().filter(r -> r.fullName().equals(name)).findFirst().orElseThrow();
    }

    @Test
    void seededBoardShowsAttentionFlags() {
        List<ResidentOverview> all = service.overview();
        assertEquals(6, all.size());
        assertEquals(BedChangeStatus.OVERDUE, byName("Franz Huber").bedStatus());
        assertEquals(BedChangeStatus.DUE_TODAY, byName("Maria Gruber").bedStatus());
        assertTrue(byName("Maria Gruber").moodDeclining());
        assertFalse(byName("Anna Berger").moodDeclining());
    }

    @Test
    void todayOnlyMedicationCanBeGivenAndChangedToNotGiven() {
        ResidentOverview maria = byName("Maria Gruber");
        MedicationView paracetamol = maria.medications().stream()
                .filter(MedicationView::onlyToday).findFirst().orElseThrow();
        assertNull(paracetamol.status());

        ResidentOverview afterGiven = service.administer(paracetamol.id(),
                new AdministerRequest(AdministrationStatus.GIVEN, null, "Tester"));
        MedicationView given = afterGiven.medications().stream()
                .filter(m -> m.id().equals(paracetamol.id())).findFirst().orElseThrow();
        assertEquals(AdministrationStatus.GIVEN, given.status());

        ResidentOverview afterChange = service.administer(paracetamol.id(),
                new AdministerRequest(AdministrationStatus.NOT_GIVEN, "Resident asleep", "Tester"));
        MedicationView changed = afterChange.medications().stream()
                .filter(m -> m.id().equals(paracetamol.id())).findFirst().orElseThrow();
        assertEquals(AdministrationStatus.NOT_GIVEN, changed.status());
        assertEquals("Resident asleep", changed.reason());
    }

    @Test
    void notGivenWithoutReasonIsRejected() {
        MedicationView pending = service.overview().stream()
                .flatMap(r -> r.medications().stream())
                .filter(m -> m.status() == null).findFirst().orElseThrow();
        assertThrows(ResponseStatusException.class, () -> service.administer(pending.id(),
                new AdministerRequest(AdministrationStatus.NOT_GIVEN, " ", "Tester")));
    }

    @Test
    void actionsRequireANurseName() {
        Long id = byName("Anna Berger").id();
        assertThrows(ResponseStatusException.class, () -> service.changeBed(id, " "));
        assertThrows(ResponseStatusException.class, () -> service.recordMood(id, new MoodRequest(3, null, null)));
    }

    @Test
    void moodTasksNotesAndBedChangeAreRecorded() {
        Long id = byName("Helga Pichler").id();

        ResidentOverview afterMood = service.recordMood(id, new MoodRequest(2, "Tearful", "Tester"));
        assertNotNull(afterMood.todayMood());
        assertEquals(2, afterMood.todayMood().score());

        ResidentOverview afterTask = service.addTask(id, new TaskRequest("Offer a warm drink", true, "Tester"));
        long openBefore = afterTask.tasks().stream().filter(t -> !t.done()).count();
        Long taskId = afterTask.tasks().stream()
                .filter(t -> t.description().equals("Offer a warm drink")).findFirst().orElseThrow().id();
        ResidentOverview afterDone = service.completeTask(taskId, "Tester");
        assertEquals(openBefore - 1, afterDone.tasks().stream().filter(t -> !t.done()).count());

        ResidentOverview afterNote = service.addNote(id,
                new NoteRequest(Shift.EARLY, "Needs help with lunch", "Tester"));
        Long noteId = afterNote.notes().stream()
                .filter(n -> n.content().equals("Needs help with lunch")).findFirst().orElseThrow().id();
        int unackBefore = afterNote.unacknowledgedNotes();
        ResidentOverview afterAck = service.acknowledgeNote(noteId, "Tester");
        assertEquals(unackBefore - 1, afterAck.unacknowledgedNotes());

        ResidentOverview afterBed = service.changeBed(id, "Tester");
        assertEquals(BedChangeStatus.OK, afterBed.bedStatus());
    }

    @Test
    void newTodayOnlyMedicationAppearsAsPending() {
        Long id = byName("Karl Steiner").id();
        ResidentOverview after = service.addMedication(id,
                new MedicationRequest("Ibuprofen", "400 mg", null, true, "Tester"));
        assertTrue(after.medications().stream()
                .anyMatch(m -> m.name().equals("Ibuprofen") && m.onlyToday() && m.status() == null));
    }

    @Test
    void activityFeedContainsRecordedChanges() {
        Long id = byName("Josef Wagner").id();
        service.changeBed(id, "Tester");
        assertTrue(service.activity(1).stream()
                .anyMatch(e -> e.eventType().equals("BED_CHANGE") && e.residentId().equals(id)));
    }
}
