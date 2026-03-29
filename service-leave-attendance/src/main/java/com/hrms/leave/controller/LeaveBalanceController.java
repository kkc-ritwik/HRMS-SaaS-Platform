package com.hrms.leave.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.leave.dto.LeaveBalanceDto;
import com.hrms.leave.service.LeaveBalanceService;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leaves/balance")
@RequiredArgsConstructor
@Tag(name = "Leave Balance", description = "View and manage leave balances")
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;

    @GetMapping("/my")
    @Operation(summary = "Get my leave balances for a given year")
    public ResponseEntity<ApiResponse<List<LeaveBalanceDto.Response>>> myBalance(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year) {
        UserPrincipal user = currentUser();
        UUID employeeId = user.getEmployeeId() != null
                ? UUID.fromString(user.getEmployeeId())
                : UUID.fromString(user.getId());
        return ResponseEntity.ok(ApiResponse.ok(
                leaveBalanceService.getBalances(tenantId(), employeeId, year)));
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('LEAVE:READ')")
    @Operation(summary = "Get leave balances for a specific employee")
    public ResponseEntity<ApiResponse<List<LeaveBalanceDto.Response>>> employeeBalance(
            @PathVariable UUID employeeId,
            @RequestParam(required = false) Integer year) {
        int balYear = year != null ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(ApiResponse.ok(
                leaveBalanceService.getBalances(tenantId(), employeeId, balYear)));
    }

    @PostMapping("/init")
    @PreAuthorize("hasAuthority('LEAVE:WRITE')")
    @Operation(summary = "Initialise yearly leave balances for an employee")
    public ResponseEntity<ApiResponse<String>> init(
            @Valid @RequestBody LeaveBalanceDto.InitRequest req) {
        if (req.getEmployeeId() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("MISSING_EMPLOYEE", "employeeId is required"));
        }
        int created = leaveBalanceService.initYearlyBalances(
                tenantId(), req.getYear(), req.getEmployeeId(), currentUserId());
        return ResponseEntity.ok(ApiResponse.ok("Initialised " + created + " balance record(s)"));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAuthority('LEAVE:APPROVE')")
    @Operation(summary = "Manually credit or debit leave days")
    public ResponseEntity<ApiResponse<LeaveBalanceDto.Response>> adjust(
            @Valid @RequestBody LeaveBalanceDto.AdjustRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                leaveBalanceService.adjustBalance(tenantId(), req, currentUserId())));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String tenantId() { return TenantContext.get(); }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
}
