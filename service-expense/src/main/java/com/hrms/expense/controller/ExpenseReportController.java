package com.hrms.expense.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.expense.dto.ExpenseReportDto;
import com.hrms.expense.service.ExpenseReportService;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
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

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expenses/reports")
@RequiredArgsConstructor
public class ExpenseReportController {

    private final ExpenseReportService service;

    @PostMapping
    @PreAuthorize("hasAuthority('EXPENSE:WRITE')")
    public ResponseEntity<ApiResponse<ExpenseReportDto.Response>> create(
            @Valid @RequestBody ExpenseReportDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        ExpenseReportDto.Response response = service.create(tenantId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EXPENSE:READ')")
    public ResponseEntity<ApiResponse<ExpenseReportDto.Response>> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(service.getById(tenantId, id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EXPENSE:READ')")
    public ResponseEntity<ApiResponse<Page<ExpenseReportDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(service.list(tenantId, pageable)));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('EXPENSE:READ')")
    public ResponseEntity<ApiResponse<Page<ExpenseReportDto.Response>>> listByEmployee(
            @PathVariable UUID employeeId,
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(service.listByEmployee(tenantId, employeeId, pageable)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EXPENSE:WRITE')")
    public ResponseEntity<ApiResponse<ExpenseReportDto.Response>> update(
            @PathVariable UUID id,
            @RequestBody ExpenseReportDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(service.update(tenantId, id, request, currentUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EXPENSE:WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        service.delete(tenantId, id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('EXPENSE:WRITE')")
    public ResponseEntity<ApiResponse<ExpenseReportDto.Response>> submit(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(service.submit(tenantId, id, currentUser)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('EXPENSE:WRITE')")
    public ResponseEntity<ApiResponse<ExpenseReportDto.Response>> approve(
            @PathVariable UUID id,
            @RequestBody Map<String, UUID> body) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        UUID approvedBy = body.get("approvedBy");
        return ResponseEntity.ok(ApiResponse.ok(service.approve(tenantId, id, approvedBy, currentUser)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('EXPENSE:WRITE')")
    public ResponseEntity<ApiResponse<ExpenseReportDto.Response>> reject(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        String reason = body.get("reason");
        return ResponseEntity.ok(ApiResponse.ok(service.reject(tenantId, id, reason, currentUser)));
    }
}
