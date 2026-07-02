package com.hrms.leave.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.leave.dto.LeaveApplicationDto;
import com.hrms.leave.entity.LeaveApplication;
import com.hrms.leave.service.LeaveApplicationService;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
@Tag(name = "Leave Applications", description = "Apply, approve, reject, cancel leaves and view team calendar")
public class LeaveApplicationController {

    private final LeaveApplicationService leaveApplicationService;

    // ── Employee endpoints ────────────────────────────────────────────────────

    @PostMapping("/apply")
    @Operation(summary = "Apply for leave")
    public ResponseEntity<ApiResponse<LeaveApplicationDto.Response>> apply(
            @Valid @RequestBody LeaveApplicationDto.ApplyRequest req) {
        UserPrincipal user = currentUser();
        UUID employeeId = user.getEmployeeId() != null
                ? UUID.fromString(user.getEmployeeId())
                : UUID.fromString(user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                leaveApplicationService.apply(tenantId(), employeeId, req, user.getId())));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my leave applications")
    public ResponseEntity<ApiResponse<List<LeaveApplicationDto.Response>>> myLeaves(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) LeaveApplication.LeaveStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        UserPrincipal user = currentUser();
        UUID employeeId = user.getEmployeeId() != null
                ? UUID.fromString(user.getEmployeeId())
                : UUID.fromString(user.getId());
        Page<LeaveApplicationDto.Response> page = leaveApplicationService.getMyLeaves(
                tenantId(), employeeId, year, status, pageable);
        PaginationMeta meta = leaveApplicationService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a leave application")
    public ResponseEntity<ApiResponse<LeaveApplicationDto.Response>> cancel(
            @PathVariable UUID id,
            @RequestBody(required = false) LeaveApplicationDto.CancelRequest req) {
        UserPrincipal user = currentUser();
        UUID employeeId = user.getEmployeeId() != null
                ? UUID.fromString(user.getEmployeeId())
                : UUID.fromString(user.getId());
        LeaveApplicationDto.CancelRequest cancelReq = req != null
                ? req : new LeaveApplicationDto.CancelRequest();
        return ResponseEntity.ok(ApiResponse.ok(
                leaveApplicationService.cancel(tenantId(), id, cancelReq, employeeId, user.getId())));
    }

    // ── Approver endpoints ────────────────────────────────────────────────────

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a leave application")
    public ResponseEntity<ApiResponse<LeaveApplicationDto.Response>> approve(
            @PathVariable UUID id,
            @RequestBody(required = false) LeaveApplicationDto.ApprovalRequest req) {
        UserPrincipal user = currentUser();
        UUID approverId = UUID.fromString(user.getId());
        LeaveApplicationDto.ApprovalRequest approvalReq = req != null
                ? req : new LeaveApplicationDto.ApprovalRequest();
        return ResponseEntity.ok(ApiResponse.ok(
                leaveApplicationService.approve(tenantId(), id, approvalReq, approverId, user.getId())));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a leave application")
    public ResponseEntity<ApiResponse<LeaveApplicationDto.Response>> reject(
            @PathVariable UUID id,
            @RequestBody(required = false) LeaveApplicationDto.ApprovalRequest req) {
        UserPrincipal user = currentUser();
        UUID approverId = UUID.fromString(user.getId());
        LeaveApplicationDto.ApprovalRequest approvalReq = req != null
                ? req : new LeaveApplicationDto.ApprovalRequest();
        return ResponseEntity.ok(ApiResponse.ok(
                leaveApplicationService.reject(tenantId(), id, approvalReq, approverId, user.getId())));
    }

    // ── Manager / HR endpoints ────────────────────────────────────────────────

    @GetMapping("/team")
    @Operation(summary = "Get team/tenant leave applications (optional employeeIds + status filter)")
    public ResponseEntity<ApiResponse<List<LeaveApplicationDto.Response>>> teamLeaves(
            @RequestParam(required = false) List<UUID> employeeIds,
            @RequestParam(required = false) com.hrms.leave.entity.LeaveApplication.LeaveStatus status,
            @RequestParam(required = false) UUID employeeId,
            @PageableDefault(size = 20) Pageable pageable) {
        if (employeeId != null) {
            employeeIds = (employeeIds == null) ? new java.util.ArrayList<>(List.of(employeeId)) : employeeIds;
            if (!employeeIds.contains(employeeId)) employeeIds.add(employeeId);
        }
        Page<LeaveApplicationDto.Response> page = leaveApplicationService
                .listTeamLeaves(tenantId(), employeeIds, status, pageable);
        PaginationMeta meta = leaveApplicationService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/team-calendar")
    @Operation(summary = "Get team leave calendar for a specific month/year")
    public ResponseEntity<ApiResponse<List<LeaveApplicationDto.TeamCalendarItem>>> teamCalendar(
            @RequestParam(required = false) List<UUID> employeeIds,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.ok(java.util.List.of()));
        }
        java.time.LocalDate now = java.time.LocalDate.now();
        int m = month != null ? month : now.getMonthValue();
        int y = year != null ? year : now.getYear();
        return ResponseEntity.ok(ApiResponse.ok(
                leaveApplicationService.getTeamCalendar(tenantId(), employeeIds, m, y)));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String tenantId() { return TenantContext.get(); }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
}
