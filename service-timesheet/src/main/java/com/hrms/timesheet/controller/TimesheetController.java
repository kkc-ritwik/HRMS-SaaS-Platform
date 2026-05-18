package com.hrms.timesheet.controller;

import com.hrms.timesheet.entity.*;
import com.hrms.timesheet.service.TimesheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/timesheet")
@RequiredArgsConstructor
public class TimesheetController {

    private final TimesheetService svc;

    @PostMapping("/projects") public Project createProject(@RequestBody Project p) { return svc.createProject(p); }
    @PostMapping("/tasks") public Task createTask(@RequestBody Task t) { return svc.createTask(t); }

    @PostMapping("/entries") public TimesheetEntry log(@RequestBody TimesheetEntry e) { return svc.logEntry(e); }
    @GetMapping("/entries") public List<TimesheetEntry> entries(@RequestParam UUID employeeId,
                                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return svc.entriesFor(employeeId, from, to);
    }

    @PostMapping("/weeks/submit") public WeeklyTimesheet submit(@RequestParam UUID employeeId,
                                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return svc.submitWeek(employeeId, weekStart);
    }
    @PostMapping("/weeks/{id}/approve") public WeeklyTimesheet approve(@org.springframework.web.bind.annotation.PathVariable UUID id,
                                                                        @RequestParam UUID approverId) {
        return svc.approveWeek(id, approverId);
    }
}
