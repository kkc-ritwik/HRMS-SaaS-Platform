package com.hrms.timesheet.reports;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/timesheet/reports/job-cost")
@RequiredArgsConstructor
public class JobCostController {

    private final JobCostReportService svc;

    @GetMapping("/project/{projectId}")
    public JobCostReportService.ProjectCost project(@PathVariable UUID projectId,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return svc.reportForProject(projectId, from, to);
    }

    @GetMapping("/tenant-summary")
    public List<JobCostReportService.ProjectCost> tenant(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate t = to != null ? to : LocalDate.now();
        LocalDate f = from != null ? from : t.minusMonths(12);
        return svc.tenantSummary(f, t);
    }
}
