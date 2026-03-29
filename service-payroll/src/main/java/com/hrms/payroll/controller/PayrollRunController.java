package com.hrms.payroll.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.payroll.dto.PayrollRunDto;
import com.hrms.payroll.service.PayrollRunService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payroll/runs")
@RequiredArgsConstructor
@Tag(name = "Payroll Runs", description = "Create, process, lock and pay payroll runs")
public class PayrollRunController {

    private final PayrollRunService payrollRunService;

    @PostMapping
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Create a new payroll run (DRAFT)")
    public ResponseEntity<ApiResponse<PayrollRunDto.Response>> create(
            @Valid @RequestBody PayrollRunDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                payrollRunService.createPayRun(tenantId(), req, currentUserId())));
    }

    @PostMapping("/{runId}/process")
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Process all active employees and generate payslips")
    public ResponseEntity<ApiResponse<PayrollRunDto.Response>> process(
            @PathVariable UUID runId) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollRunService.processPayRun(tenantId(), runId, currentUserId())));
    }

    @PostMapping("/{runId}/lock")
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Lock a processed payroll run (freezes all payslips)")
    public ResponseEntity<ApiResponse<PayrollRunDto.Response>> lock(
            @PathVariable UUID runId) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollRunService.lockPayRun(tenantId(), runId, currentUserId())));
    }

    @PostMapping("/{runId}/mark-paid")
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Mark a locked payroll run as PAID (publishes payslips to employees)")
    public ResponseEntity<ApiResponse<PayrollRunDto.Response>> markPaid(
            @PathVariable UUID runId) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollRunService.markAsPaid(tenantId(), runId, currentUserId())));
    }

    @GetMapping("/{runId}")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get a payroll run by ID")
    public ResponseEntity<ApiResponse<PayrollRunDto.Response>> get(
            @PathVariable UUID runId) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollRunService.getPayRun(tenantId(), runId)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "List all payroll runs (paginated, newest first)")
    public ResponseEntity<ApiResponse<List<PayrollRunDto.Response>>> list(
            @PageableDefault(size = 12) Pageable pageable) {
        Page<PayrollRunDto.Response> page = payrollRunService.listPayRuns(tenantId(), pageable);
        PaginationMeta meta = payrollRunService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    private String tenantId() { return TenantContext.get(); }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
