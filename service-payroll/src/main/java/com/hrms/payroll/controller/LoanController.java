package com.hrms.payroll.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.payroll.dto.LoanDto;
import com.hrms.payroll.service.LoanService;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payroll/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "Employee loan and salary advance management")
public class LoanController {

    private final LoanService loanService;

    // ── Admin: create loan for any employee ──────────────────────────────────

    @PostMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Create a loan / salary advance for an employee")
    public ResponseEntity<ApiResponse<LoanDto.Response>> create(
            @PathVariable UUID employeeId,
            @Valid @RequestBody LoanDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                loanService.create(tenantId(), employeeId, req, currentUserId())));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get all loans for an employee")
    public ResponseEntity<ApiResponse<List<LoanDto.Response>>> getEmployeeLoans(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(
                loanService.getMyLoans(tenantId(), employeeId)));
    }

    @GetMapping("/{loanId}")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get a loan by ID")
    public ResponseEntity<ApiResponse<LoanDto.Response>> get(
            @PathVariable UUID loanId) {
        return ResponseEntity.ok(ApiResponse.ok(
                loanService.getLoan(tenantId(), loanId)));
    }

    @GetMapping("/{loanId}/schedule")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get the full amortization / repayment schedule for a loan")
    public ResponseEntity<ApiResponse<List<LoanDto.RepaymentScheduleItem>>> schedule(
            @PathVariable UUID loanId) {
        return ResponseEntity.ok(ApiResponse.ok(
                loanService.getRepaymentSchedule(tenantId(), loanId)));
    }

    @PostMapping("/{loanId}/close")
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Close (foreclose) a loan")
    public ResponseEntity<ApiResponse<LoanDto.Response>> close(
            @PathVariable UUID loanId) {
        return ResponseEntity.ok(ApiResponse.ok(
                loanService.closeLoan(tenantId(), loanId, currentUserId())));
    }

    // ── Employee self-service ─────────────────────────────────────────────────

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get my loans")
    public ResponseEntity<ApiResponse<List<LoanDto.Response>>> getMyLoans() {
        return ResponseEntity.ok(ApiResponse.ok(
                loanService.getMyLoans(tenantId(), employeeId())));
    }

    @GetMapping("/me/{loanId}/schedule")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get repayment schedule for my loan")
    public ResponseEntity<ApiResponse<List<LoanDto.RepaymentScheduleItem>>> mySchedule(
            @PathVariable UUID loanId) {
        return ResponseEntity.ok(ApiResponse.ok(
                loanService.getRepaymentSchedule(tenantId(), loanId)));
    }

    private String tenantId() { return TenantContext.get(); }

    private UUID employeeId() {
        return UUID.fromString(
                ((UserPrincipal) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal()).getEmployeeId());
    }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
