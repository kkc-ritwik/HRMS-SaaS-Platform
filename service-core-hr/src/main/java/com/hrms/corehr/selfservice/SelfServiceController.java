package com.hrms.corehr.selfservice;

import com.hrms.corehr.entity.Employee;
import com.hrms.corehr.repository.EmployeeRepository;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Unified self-service endpoints — single API call returns everything the employee's
 * home page needs. The employee/manager/user id defaults to the authenticated principal,
 * so the frontend can call these with no params. Returns empties when the caller has no
 * employee record (e.g. an admin user) rather than failing.
 */
@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class SelfServiceController {

    private final EmployeeRepository employees;

    private UserPrincipal principal() {
        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (p instanceof UserPrincipal up) ? up : null;
    }

    private UUID resolve(UUID provided, String fromPrincipal) {
        if (provided != null) return provided;
        if (fromPrincipal == null || fromPrincipal.isBlank()) return null;
        try { return UUID.fromString(fromPrincipal); } catch (Exception e) { return null; }
    }

    /** Compact home-page payload. */
    @GetMapping("/dashboard")
    public Map<String, Object> myDashboard(@RequestParam(required = false) UUID employeeId) {
        UserPrincipal me = principal();
        UUID empId = resolve(employeeId, me == null ? null : me.getEmployeeId());
        Map<String, Object> dash = new LinkedHashMap<>();
        Optional<Employee> opt = empId == null ? Optional.empty()
                : employees.findByIdAndTenantIdAndDeletedFalse(empId, TenantContext.get());
        if (opt.isPresent()) {
            Employee e = opt.get();
            dash.put("profile", Map.of(
                    "id", e.getId(), "employeeCode", e.getEmployeeCode() == null ? "" : e.getEmployeeCode(),
                    "displayName", e.getDisplayName() != null ? e.getDisplayName()
                            : (e.getFirstName() + " " + e.getLastName()),
                    "designationId", e.getDesignationId() == null ? "" : e.getDesignationId(),
                    "departmentId",  e.getDepartmentId()  == null ? "" : e.getDepartmentId(),
                    "managerId",     e.getManagerId()     == null ? "" : e.getManagerId(),
                    "joinDate", e.getJoinDate() == null ? "" : e.getJoinDate(),
                    "yearsOfService", e.getJoinDate() == null ? 0
                            : ChronoUnit.YEARS.between(e.getJoinDate(), LocalDate.now()),
                    "photoUrl", e.getProfilePictureUrl() == null ? "" : e.getProfilePictureUrl()));
        } else {
            dash.put("profile", Map.of("displayName", me == null ? "" : (me.getFullName() == null ? "" : me.getFullName())));
        }
        dash.put("sectionsAvailable",
                List.of("leaveBalance","pendingApprovals","upcomingTimeOff","lastPayslip","todayCelebrations","quickLinks"));
        return dash;
    }

    /** Manager team view — direct reports. */
    @GetMapping("/team")
    public Map<String, Object> myTeam(@RequestParam(required = false) UUID managerId) {
        UserPrincipal me = principal();
        UUID mgrId = resolve(managerId, me == null ? null : me.getEmployeeId());
        List<Employee> reports = mgrId == null ? List.of()
                : employees.findByManagerIdAndTenantIdAndDeletedFalse(mgrId, TenantContext.get());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("managerId", mgrId);
        out.put("directReportCount", reports.size());
        out.put("directReports", reports.stream().map(r -> Map.of(
                "id", r.getId(),
                "employeeCode", r.getEmployeeCode() == null ? "" : r.getEmployeeCode(),
                "name", r.getFirstName() + " " + r.getLastName(),
                "designationId", r.getDesignationId() == null ? "" : r.getDesignationId(),
                "employmentStatus", r.getEmploymentStatus(),
                "joinDate", r.getJoinDate() == null ? "" : r.getJoinDate(),
                "photoUrl", r.getProfilePictureUrl() == null ? "" : r.getProfilePictureUrl()
        )).toList());
        return out;
    }

    /** Unified approval inbox — placeholder aggregator. */
    @GetMapping("/approvals")
    public Map<String, Object> myApprovals(@RequestParam(required = false) UUID userId) {
        UserPrincipal me = principal();
        UUID uid = resolve(userId, me == null ? null : me.getId());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("userId", uid);
        out.put("counts", Map.of(
                "leave", 0, "expense", 0, "travel", 0, "timesheet", 0,
                "hiring", 0, "asset", 0, "wfh", 0, "compOff", 0));
        out.put("items", List.of());
        return out;
    }
}
