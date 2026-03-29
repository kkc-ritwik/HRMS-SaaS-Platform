package com.hrms.payroll.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.payroll.dto.SalaryStructureDto;
import com.hrms.payroll.service.SalaryStructureService;
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
@RequestMapping("/api/v1/salary/structures")
@RequiredArgsConstructor
@Tag(name = "Salary Structures", description = "Manage salary structure templates and their component assignments")
public class SalaryStructureController {

    private final SalaryStructureService structureService;

    @PostMapping
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Create a salary structure")
    public ResponseEntity<ApiResponse<SalaryStructureDto.Response>> create(
            @Valid @RequestBody SalaryStructureDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(structureService.create(tenantId(), req, currentUserId())));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "List salary structures (paginated)")
    public ResponseEntity<ApiResponse<List<SalaryStructureDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SalaryStructureDto.Response> page = structureService.list(tenantId(), pageable);
        PaginationMeta meta = structureService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/active")
    @Operation(summary = "List active salary structures (for dropdowns)")
    public ResponseEntity<ApiResponse<List<SalaryStructureDto.Response>>> listActive() {
        return ResponseEntity.ok(ApiResponse.ok(structureService.listActive(tenantId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get a salary structure by ID (includes component list)")
    public ResponseEntity<ApiResponse<SalaryStructureDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(structureService.getById(tenantId(), id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Update a salary structure")
    public ResponseEntity<ApiResponse<SalaryStructureDto.Response>> update(
            @PathVariable UUID id,
            @RequestBody SalaryStructureDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                structureService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Delete a salary structure")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        structureService.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    // ── Component management ──────────────────────────────────────────────────

    @PostMapping("/{id}/components")
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Add or update a component in a salary structure")
    public ResponseEntity<ApiResponse<SalaryStructureDto.Response>> addComponent(
            @PathVariable UUID id,
            @Valid @RequestBody SalaryStructureDto.ComponentAssignRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                structureService.addComponent(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}/components/{componentId}")
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Remove a component from a salary structure")
    public ResponseEntity<Void> removeComponent(
            @PathVariable UUID id,
            @PathVariable UUID componentId) {
        structureService.removeComponent(tenantId(), id, componentId);
        return ResponseEntity.noContent().build();
    }

    private String tenantId()     { return TenantContext.get(); }
    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
