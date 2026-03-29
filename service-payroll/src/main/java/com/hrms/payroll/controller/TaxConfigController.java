package com.hrms.payroll.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.payroll.dto.TaxConfigDto;
import com.hrms.payroll.service.TaxConfigService;
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
@RequestMapping("/api/v1/salary/tax-config")
@RequiredArgsConstructor
@Tag(name = "Tax Configuration", description = "Manage PF, ESI, and Professional Tax slab configurations")
public class TaxConfigController {

    private final TaxConfigService taxConfigService;

    // ── PF ────────────────────────────────────────────────────────────────────

    @PostMapping("/pf")
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Save a new PF configuration (creates a new effective-from record)")
    public ResponseEntity<ApiResponse<TaxConfigDto.PfConfigResponse>> savePfConfig(
            @Valid @RequestBody TaxConfigDto.PfConfigRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                taxConfigService.savePfConfig(tenantId(), req, currentUserId())));
    }

    @GetMapping("/pf")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get current (latest effective) PF configuration")
    public ResponseEntity<ApiResponse<TaxConfigDto.PfConfigResponse>> getCurrentPfConfig() {
        return ResponseEntity.ok(ApiResponse.ok(
                taxConfigService.getCurrentPfConfig(tenantId())));
    }

    // ── ESI ───────────────────────────────────────────────────────────────────

    @PostMapping("/esi")
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Save a new ESI configuration")
    public ResponseEntity<ApiResponse<TaxConfigDto.EsiConfigResponse>> saveEsiConfig(
            @Valid @RequestBody TaxConfigDto.EsiConfigRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                taxConfigService.saveEsiConfig(tenantId(), req, currentUserId())));
    }

    @GetMapping("/esi")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get current (latest effective) ESI configuration")
    public ResponseEntity<ApiResponse<TaxConfigDto.EsiConfigResponse>> getCurrentEsiConfig() {
        return ResponseEntity.ok(ApiResponse.ok(
                taxConfigService.getCurrentEsiConfig(tenantId())));
    }

    // ── PT Slabs ──────────────────────────────────────────────────────────────

    @PostMapping("/pt")
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Add a Professional Tax slab")
    public ResponseEntity<ApiResponse<TaxConfigDto.PtSlabResponse>> createPtSlab(
            @Valid @RequestBody TaxConfigDto.PtSlabRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                taxConfigService.createPtSlab(tenantId(), req, currentUserId())));
    }

    @GetMapping("/pt/{state}")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get PT slabs for a state (e.g. KARNATAKA, MAHARASHTRA)")
    public ResponseEntity<ApiResponse<List<TaxConfigDto.PtSlabResponse>>> getPtSlabs(
            @PathVariable String state) {
        return ResponseEntity.ok(ApiResponse.ok(
                taxConfigService.getPtSlabsByState(tenantId(), state)));
    }

    @DeleteMapping("/pt/{id}")
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Delete a PT slab")
    public ResponseEntity<Void> deletePtSlab(@PathVariable UUID id) {
        taxConfigService.deletePtSlab(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    private String tenantId()     { return TenantContext.get(); }
    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
