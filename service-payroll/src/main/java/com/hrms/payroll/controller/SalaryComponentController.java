package com.hrms.payroll.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.payroll.dto.SalaryComponentDto;
import com.hrms.payroll.service.SalaryComponentService;
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
@RequestMapping("/api/v1/salary/components")
@RequiredArgsConstructor
@Tag(name = "Salary Components", description = "Manage earnings, deductions, reimbursements and employer contributions")
public class SalaryComponentController {

    private final SalaryComponentService componentService;

    @PostMapping
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Create a salary component")
    public ResponseEntity<ApiResponse<SalaryComponentDto.Response>> create(
            @Valid @RequestBody SalaryComponentDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(componentService.create(tenantId(), req, currentUserId())));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "List all salary components (paginated)")
    public ResponseEntity<ApiResponse<List<SalaryComponentDto.Response>>> list(
            @PageableDefault(size = 50) Pageable pageable) {
        Page<SalaryComponentDto.Response> page = componentService.list(tenantId(), pageable);
        PaginationMeta meta = componentService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/active")
    @Operation(summary = "List active salary components (for dropdowns)")
    public ResponseEntity<ApiResponse<List<SalaryComponentDto.Response>>> listActive() {
        return ResponseEntity.ok(ApiResponse.ok(componentService.listActive(tenantId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get a salary component by ID")
    public ResponseEntity<ApiResponse<SalaryComponentDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(componentService.getById(tenantId(), id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Update a salary component")
    public ResponseEntity<ApiResponse<SalaryComponentDto.Response>> update(
            @PathVariable UUID id,
            @RequestBody SalaryComponentDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                componentService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Soft-delete a salary component")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        componentService.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    private String tenantId()     { return TenantContext.get(); }
    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
