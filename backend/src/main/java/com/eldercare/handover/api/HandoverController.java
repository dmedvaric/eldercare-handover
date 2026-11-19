package com.eldercare.handover.api;

import com.eldercare.handover.api.Dtos.ActivityView;
import com.eldercare.handover.api.Dtos.AdministerRequest;
import com.eldercare.handover.api.Dtos.MedicationRequest;
import com.eldercare.handover.api.Dtos.MoodRequest;
import com.eldercare.handover.api.Dtos.NoteRequest;
import com.eldercare.handover.api.Dtos.NurseRequest;
import com.eldercare.handover.api.Dtos.ResidentOverview;
import com.eldercare.handover.api.Dtos.TaskRequest;
import com.eldercare.handover.service.HandoverService;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class HandoverController {

    private final HandoverService service;

    public HandoverController(HandoverService service) {
        this.service = service;
    }

    @GetMapping("/residents")
    public List<ResidentOverview> residents() {
        return service.overview();
    }

    @GetMapping("/activity")
    public List<ActivityView> activity(@RequestParam(defaultValue = "8") int hours) {
        return service.activity(hours);
    }

    @PostMapping("/residents/{id}/bed-change")
    public ResidentOverview changeBed(@PathVariable Long id, @RequestBody NurseRequest body) {
        return service.changeBed(id, body.nurse());
    }

    @PostMapping("/residents/{id}/mood")
    public ResidentOverview recordMood(@PathVariable Long id, @RequestBody MoodRequest body) {
        return service.recordMood(id, body);
    }

    @PostMapping("/residents/{id}/medications")
    public ResidentOverview addMedication(@PathVariable Long id, @RequestBody MedicationRequest body) {
        return service.addMedication(id, body);
    }

    @PostMapping("/medications/{id}/administer")
    public ResidentOverview administer(@PathVariable Long id, @RequestBody AdministerRequest body) {
        return service.administer(id, body);
    }

    @PostMapping("/residents/{id}/tasks")
    public ResidentOverview addTask(@PathVariable Long id, @RequestBody TaskRequest body) {
        return service.addTask(id, body);
    }

    @PostMapping("/tasks/{id}/complete")
    public ResidentOverview completeTask(@PathVariable Long id, @RequestBody NurseRequest body) {
        return service.completeTask(id, body.nurse());
    }

    @PostMapping("/residents/{id}/notes")
    public ResidentOverview addNote(@PathVariable Long id, @RequestBody NoteRequest body) {
        return service.addNote(id, body);
    }

    @PostMapping("/notes/{id}/acknowledge")
    public ResidentOverview acknowledgeNote(@PathVariable Long id, @RequestBody NurseRequest body) {
        return service.acknowledgeNote(id, body.nurse());
    }
}
