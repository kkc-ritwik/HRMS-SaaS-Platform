package com.hrms.timesheet.reports;

import com.hrms.security.model.TenantContext;
import com.hrms.timesheet.entity.Project;
import com.hrms.timesheet.entity.TimesheetEntry;
import com.hrms.timesheet.repository.TimesheetRepositories;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Aggregates timesheet entries into a project / job-cost report:
 *   per project → { billableHours, nonBillableHours, totalHours,
 *                   billableValue (hours × billingRate), perEmployeeBreakdown }
 *
 * Used by Finance to invoice clients and PMs to track project burn vs budget.
 */
@Service
@RequiredArgsConstructor
public class JobCostReportService {

    private final TimesheetRepositories.ProjectRepository projects;
    private final TimesheetRepositories.TimesheetEntryRepository entries;

    public ProjectCost reportForProject(UUID projectId, LocalDate from, LocalDate to) {
        Project p = projects.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        List<TimesheetEntry> rows = entries.findByTenantIdAndProjectIdAndWorkDateBetween(
                TenantContext.get(), projectId, from, to);

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal billable = BigDecimal.ZERO;
        Map<UUID, BigDecimal> totalByEmployee = new HashMap<>();
        Map<UUID, BigDecimal> billableByEmployee = new HashMap<>();

        for (TimesheetEntry r : rows) {
            BigDecimal h = r.getHours() == null ? BigDecimal.ZERO : r.getHours();
            total = total.add(h);
            totalByEmployee.merge(r.getEmployeeId(), h, BigDecimal::add);
            if (Boolean.TRUE.equals(r.getIsBillable())) {
                billable = billable.add(h);
                billableByEmployee.merge(r.getEmployeeId(), h, BigDecimal::add);
            }
        }
        BigDecimal billableValue = (p.getBillingRate() == null) ? BigDecimal.ZERO
                : billable.multiply(p.getBillingRate()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal nonBillable = total.subtract(billable);

        List<EmployeeContribution> contributions = new ArrayList<>();
        for (var e : totalByEmployee.entrySet()) {
            BigDecimal eTotal = e.getValue();
            BigDecimal eBillable = billableByEmployee.getOrDefault(e.getKey(), BigDecimal.ZERO);
            contributions.add(new EmployeeContribution(e.getKey(), eTotal, eBillable, eTotal.subtract(eBillable)));
        }
        contributions.sort((a, b) -> b.totalHours().compareTo(a.totalHours()));

        return new ProjectCost(
                p.getId(), p.getName(), p.getCode(), p.getClient(),
                p.getBillingRate(), p.getCurrency(),
                from, to,
                total, billable, nonBillable, billableValue,
                contributions);
    }

    /** Tenant-wide cost across all active projects in the period. */
    public List<ProjectCost> tenantSummary(LocalDate from, LocalDate to) {
        List<ProjectCost> out = new ArrayList<>();
        // findAll is intentional — small project counts (10s-100s) per tenant.
        for (Project p : projects.findAll()) {
            if (!TenantContext.get().equals(p.getTenantId())) continue;
            out.add(reportForProject(p.getId(), from, to));
        }
        out.sort((a, b) -> b.totalHours().compareTo(a.totalHours()));
        return out;
    }

    public record ProjectCost(UUID projectId, String name, String code, String client,
                              BigDecimal billingRate, String currency,
                              LocalDate from, LocalDate to,
                              BigDecimal totalHours, BigDecimal billableHours, BigDecimal nonBillableHours,
                              BigDecimal billableValue,
                              List<EmployeeContribution> contributions) {}

    public record EmployeeContribution(UUID employeeId, BigDecimal totalHours,
                                        BigDecimal billableHours, BigDecimal nonBillableHours) {}
}
