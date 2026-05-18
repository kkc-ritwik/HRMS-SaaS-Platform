package com.hrms.timesheet.service;

import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import com.hrms.events.publisher.EventPublisher;
import com.hrms.security.model.TenantContext;
import com.hrms.timesheet.entity.*;
import com.hrms.timesheet.repository.TimesheetRepositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimesheetService {

    private final ProjectRepository projects;
    private final TaskRepository tasks;
    private final TimesheetEntryRepository entries;
    private final WeeklyTimesheetRepository weeklies;
    private final EventPublisher events;

    @Transactional public Project createProject(Project p) {
        p.setTenantId(TenantContext.get()); return projects.save(p);
    }
    @Transactional public Task createTask(Task t) {
        t.setTenantId(TenantContext.get()); t.setIsActive(true); return tasks.save(t);
    }

    @Transactional public TimesheetEntry logEntry(TimesheetEntry e) {
        e.setTenantId(TenantContext.get());
        if (e.getStatus() == null) e.setStatus(TimesheetEntry.Status.DRAFT);
        return entries.save(e);
    }

    public List<TimesheetEntry> entriesFor(UUID employeeId, LocalDate from, LocalDate to) {
        return entries.findByTenantIdAndEmployeeIdAndWorkDateBetween(TenantContext.get(), employeeId, from, to);
    }

    @Transactional public WeeklyTimesheet submitWeek(UUID employeeId, LocalDate weekStart) {
        List<TimesheetEntry> rows = entriesFor(employeeId, weekStart, weekStart.plusDays(6));
        BigDecimal total = rows.stream().map(r -> r.getHours() == null ? BigDecimal.ZERO : r.getHours())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal billable = rows.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsBillable()))
                .map(r -> r.getHours() == null ? BigDecimal.ZERO : r.getHours())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        WeeklyTimesheet w = weeklies
                .findByTenantIdAndEmployeeIdAndWeekStart(TenantContext.get(), employeeId, weekStart)
                .orElseGet(() -> WeeklyTimesheet.builder()
                        .tenantId(TenantContext.get()).employeeId(employeeId).weekStart(weekStart).build());
        w.setTotalHours(total);
        w.setBillableHours(billable);
        w.setStatus(WeeklyTimesheet.Status.SUBMITTED);
        w.setSubmittedAt(OffsetDateTime.now());
        rows.forEach(r -> r.setStatus(TimesheetEntry.Status.SUBMITTED));
        entries.saveAll(rows);
        WeeklyTimesheet saved = weeklies.save(w);
        events.publish(Topics.TIMESHEET, DomainEvent.of("timesheet.submitted", "timesheet",
                w.getTenantId(), saved.getId().toString(), "WeeklyTimesheet",
                Map.of("employeeId", employeeId, "totalHours", total)));
        return saved;
    }

    @Transactional public WeeklyTimesheet approveWeek(UUID weeklyId, UUID approverId) {
        WeeklyTimesheet w = weeklies.findById(weeklyId).orElseThrow();
        w.setStatus(WeeklyTimesheet.Status.APPROVED);
        w.setApproverId(approverId);
        w.setApprovedAt(OffsetDateTime.now());
        events.publish(Topics.TIMESHEET, DomainEvent.of("timesheet.approved", "timesheet",
                w.getTenantId(), weeklyId.toString(), "WeeklyTimesheet", Map.of("approverId", approverId)));
        return weeklies.save(w);
    }
}
