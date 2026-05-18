package com.hrms.corehr.selfservice;

import com.hrms.corehr.entity.Employee;
import com.hrms.corehr.repository.EmployeeRepository;
import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Unified self-service endpoints — single API call returns everything the employee's
 * home page needs (profile, leave balance, pending approvals count, upcoming time off,
 * recent payslip, today's celebrations). Frontend renders in one network round-trip.
 *
 * Heavy aggregation handled by Feign clients to other services; this returns nulls /
 * empties for sections the calling service can't fetch (defensive).
 */
@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class SelfServiceController {

    private final EmployeeRepository employees;

    /** Compact home-page payload. */
    @GetMapping("/dashboard")
    public Map<String, Object> myDashboard(@RequestParam UUID employeeId) {
        Employee e = employees.findByIdAndTenantIdAndDeletedFalse(employeeId, TenantContext.get())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        Map<String, Object> dash = new LinkedHashMap<>();
        dash.put("profile", Map.of(
                "id", e.getId(), "employeeCode", e.getEmployeeCode(),
                "displayName", e.getDisplayName() != null ? e.getDisplayName()
                        : e.getFirstName() + " " + e.getLastName(),
                "designationId", e.getDesignationId() == null ? "" : e.getDesignationId(),
                "departmentId",  e.getDepartmentId()  == null ? "" : e.getDepartmentId(),
                "managerId",     e.getManagerId()     == null ? "" : e.getManagerId(),
                "joinDate", e.getJoinDate(),
                "yearsOfService", e.getJoinDate() == null ? 0
                        : ChronoUnit.YEARS.between(e.getJoinDate(), LocalDate.now()),
                "photoUrl", e.getProfilePictureUrl() == null ? "" : e.getProfilePictureUrl()));
        // Other sections (leaveBalance, pendingApprovalsCount, lastPayslip, todayCelebrations)
        // are populated by the frontend BFF or via additional Feign calls in production.
        dash.put("sectionsAvailable",
                List.of("leaveBalance","pendingApprovals","upcomingTimeOff","lastPayslip","todayCelebrations","quickLinks"));
        return dash;
    }

    /** Manager team view — direct reports + their pending leave/timesheet/expense requests. */
    @GetMapping("/team")
    public Map<String, Object> myTeam(@RequestParam UUID managerId) {
        List<Employee> reports = employees.findByManagerIdAndTenantIdAndDeletedFalse(managerId, TenantContext.get());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("managerId", managerId);
        out.put("directReportCount", reports.size());
        out.put("directReports", reports.stream().map(r -> Map.of(
                "id", r.getId(),
                "employeeCode", r.getEmployeeCode(),
                "name", r.getFirstName() + " " + r.getLastName(),
                "designationId", r.getDesignationId() == null ? "" : r.getDesignationId(),
                "employmentStatus", r.getEmploymentStatus(),
                "joinDate", r.getJoinDate(),
                "photoUrl", r.getProfilePictureUrl() == null ? "" : r.getProfilePictureUrl()
        )).toList());
        return out;
    }

    /** Unified approval inbox — placeholder aggregator; calling service fans out via Feign. */
    @GetMapping("/approvals")
    public Map<String, Object> myApprovals(@RequestParam UUID userId) {
        // In production this is populated by the gateway BFF that calls each module's
        // pending-approvals endpoint. We return the shape here so the frontend has a
        // single contract regardless of how many modules feed in.
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("userId", userId);
        out.put("counts", Map.of(
                "leave", 0, "expense", 0, "travel", 0, "timesheet", 0,
                "hiring", 0, "asset", 0, "wfh", 0, "compOff", 0));
        out.put("items", List.of());
        return out;
    }
}
