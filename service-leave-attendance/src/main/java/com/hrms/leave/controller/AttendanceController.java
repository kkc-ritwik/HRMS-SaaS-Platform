package com.hrms.leave.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.leave.dto.AttendanceDto;
import com.hrms.leave.dto.RegularizationDto;
import com.hrms.leave.service.AttendanceService;
import com.hrms.leave.service.RegularizationService;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Punch in/out, attendance logs, team view, dashboard, and regularization")
public class AttendanceController {

    private final AttendanceService      attendanceService;
    private final RegularizationService  regularizationService;

    // ── Punch (auto-detect IN / OUT) ──────────────────────────────────────────

    @PostMapping("/punch")
    @Operation(summary = "Punch in or out (system auto-detects direction)")
    public ResponseEntity<ApiResponse<AttendanceDto.PunchResponse>> punch(
            @RequestBody(required = false) AttendanceDto.PunchRequest req) {
        UserPrincipal user = currentUser();
        UUID employeeId = resolveEmployeeId(user);
        AttendanceDto.PunchRequest request = req != null ? req : new AttendanceDto.PunchRequest();
        return ResponseEntity.ok(ApiResponse.ok(
                attendanceService.punch(tenantId(), employeeId, request, user.getId())));
    }

    // ── My Attendance ─────────────────────────────────────────────────────────

    @GetMapping("/my-log")
    @Operation(summary = "Get my monthly attendance summary")
    public ResponseEntity<ApiResponse<AttendanceDto.MonthSummary>> myLog(
            @RequestParam int year,
            @RequestParam int month) {
        UserPrincipal user = currentUser();
        UUID employeeId = resolveEmployeeId(user);
        return ResponseEntity.ok(ApiResponse.ok(
                attendanceService.getMyAttendance(tenantId(), employeeId, year, month)));
    }

    @GetMapping("/my-records")
    @Operation(summary = "Get my attendance records (paginated)")
    public ResponseEntity<ApiResponse<List<AttendanceDto.RecordResponse>>> myRecords(
            @PageableDefault(size = 20) Pageable pageable) {
        UserPrincipal user = currentUser();
        UUID employeeId = resolveEmployeeId(user);
        Page<AttendanceDto.RecordResponse> page =
                attendanceService.getAttendancePage(tenantId(), employeeId, pageable);
        PaginationMeta meta = PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize())
                .total(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    // ── Team Attendance ────────────────────────────────────────────────────────

    @GetMapping("/team")
    @PreAuthorize("hasAuthority('ATTENDANCE:READ')")
    @Operation(summary = "Get team attendance for a specific date")
    public ResponseEntity<ApiResponse<List<AttendanceDto.TeamAttendanceItem>>> teamAttendance(
            @RequestParam List<UUID> employeeIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(
                attendanceService.getTeamAttendance(tenantId(), employeeIds, targetDate)));
    }

    // ── Dashboard ──────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ATTENDANCE:READ')")
    @Operation(summary = "Attendance dashboard — who is in, late, absent today")
    public ResponseEntity<ApiResponse<AttendanceDto.DashboardResponse>> dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(
                attendanceService.getDashboard(tenantId(), targetDate)));
    }

    // ── Regularization ─────────────────────────────────────────────────────────

    @PostMapping("/regularize")
    @Operation(summary = "Submit a regularization request")
    public ResponseEntity<ApiResponse<RegularizationDto.Response>> regularize(
            @Valid @RequestBody RegularizationDto.RequestDto req) {
        UserPrincipal user = currentUser();
        UUID employeeId = resolveEmployeeId(user);
        return ResponseEntity.ok(ApiResponse.ok(
                regularizationService.request(tenantId(), employeeId, req, user.getId())));
    }

    @GetMapping("/regularize")
    @Operation(summary = "Get my regularization requests")
    public ResponseEntity<ApiResponse<List<RegularizationDto.Response>>> myRegularizations(
            @PageableDefault(size = 20) Pageable pageable) {
        UserPrincipal user = currentUser();
        UUID employeeId = resolveEmployeeId(user);
        Page<RegularizationDto.Response> page =
                regularizationService.getMyRequests(tenantId(), employeeId, pageable);
        PaginationMeta meta = regularizationService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/regularize/pending")
    @PreAuthorize("hasAuthority('ATTENDANCE:APPROVE')")
    @Operation(summary = "Get all pending regularization requests (HR / manager)")
    public ResponseEntity<ApiResponse<List<RegularizationDto.Response>>> pendingRegularizations() {
        return ResponseEntity.ok(ApiResponse.ok(
                regularizationService.getPendingRequests(tenantId())));
    }

    @PostMapping("/regularize/{id}/approve")
    @PreAuthorize("hasAuthority('ATTENDANCE:APPROVE')")
    @Operation(summary = "Approve a regularization request")
    public ResponseEntity<ApiResponse<RegularizationDto.Response>> approveRegularization(
            @PathVariable UUID id,
            @RequestBody(required = false) RegularizationDto.ApprovalDto req) {
        UserPrincipal user = currentUser();
        RegularizationDto.ApprovalDto body = req != null ? req : new RegularizationDto.ApprovalDto();
        return ResponseEntity.ok(ApiResponse.ok(
                regularizationService.approve(tenantId(), id, body,
                        UUID.fromString(user.getId()), user.getId())));
    }

    @PostMapping("/regularize/{id}/reject")
    @PreAuthorize("hasAuthority('ATTENDANCE:APPROVE')")
    @Operation(summary = "Reject a regularization request")
    public ResponseEntity<ApiResponse<RegularizationDto.Response>> rejectRegularization(
            @PathVariable UUID id,
            @RequestBody(required = false) RegularizationDto.ApprovalDto req) {
        UserPrincipal user = currentUser();
        RegularizationDto.ApprovalDto body = req != null ? req : new RegularizationDto.ApprovalDto();
        return ResponseEntity.ok(ApiResponse.ok(
                regularizationService.reject(tenantId(), id, body,
                        UUID.fromString(user.getId()), user.getId())));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String tenantId() { return TenantContext.get(); }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private UUID resolveEmployeeId(UserPrincipal user) {
        return user.getEmployeeId() != null
                ? UUID.fromString(user.getEmployeeId())
                : UUID.fromString(user.getId());
    }
}
