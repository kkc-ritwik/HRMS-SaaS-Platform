package com.hrms.payroll.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.payroll.dto.PayslipDto;
import com.hrms.payroll.service.PayslipService;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payslips")
@RequiredArgsConstructor
@Tag(name = "Payslips", description = "View and download payslips")
public class PayslipController {

    private final PayslipService payslipService;

    // ── Employee self-service ─────────────────────────────────────────────────

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get my payslips (paginated)")
    public ResponseEntity<ApiResponse<List<PayslipDto.Summary>>> getMyPayslips(
            @PageableDefault(size = 12) Pageable pageable) {
        UUID employeeId = employeeId();
        if (employeeId == null) return ResponseEntity.ok(ApiResponse.ok(List.of()));
        Page<PayslipDto.Summary> page = payslipService.getMyPayslips(tenantId(), employeeId, pageable);
        PaginationMeta meta = payslipService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/me/{payslipId}")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get a specific payslip detail")
    public ResponseEntity<ApiResponse<PayslipDto.Response>> getMyPayslip(
            @PathVariable UUID payslipId) {
        return ResponseEntity.ok(ApiResponse.ok(
                payslipService.getPayslipById(tenantId(), payslipId)));
    }

    @GetMapping("/me/{payslipId}/pdf")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get PDF URL for a payslip")
    public ResponseEntity<ApiResponse<String>> getPayslipPdf(
            @PathVariable UUID payslipId) {
        return ResponseEntity.ok(ApiResponse.ok(
                payslipService.generatePayslipPdf(tenantId(), payslipId)));
    }

    // ── Admin / payroll manager ────────────────────────────────────────────────

    @GetMapping("/run/{runId}")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get all payslips for a payroll run")
    public ResponseEntity<ApiResponse<List<PayslipDto.Response>>> getRunPayslips(
            @PathVariable UUID runId) {
        return ResponseEntity.ok(ApiResponse.ok(
                payslipService.getRunPayslips(tenantId(), runId)));
    }

    @GetMapping("/{payslipId}")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get a payslip by ID (admin)")
    public ResponseEntity<ApiResponse<PayslipDto.Response>> getById(
            @PathVariable UUID payslipId) {
        return ResponseEntity.ok(ApiResponse.ok(
                payslipService.getPayslipById(tenantId(), payslipId)));
    }

    private String tenantId() { return TenantContext.get(); }

    private UUID employeeId() {
        String eid = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getEmployeeId();
        return (eid == null || eid.isBlank()) ? null : UUID.fromString(eid);
    }
}
