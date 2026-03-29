package com.hrms.expense.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.expense.dto.AdvanceDto;
import com.hrms.expense.service.AdvanceService;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expenses/advances")
@RequiredArgsConstructor
public class AdvanceController {

    private final AdvanceService service;

    @PostMapping
    @PreAuthorize("hasAuthority('EXPENSE:WRITE')")
    public ResponseEntity<ApiResponse<AdvanceDto.Response>> create(
            @Valid @RequestBody AdvanceDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        AdvanceDto.Response response = service.create(tenantId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EXPENSE:READ')")
    public ResponseEntity<ApiResponse<AdvanceDto.Response>> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(service.getById(tenantId, id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EXPENSE:READ')")
    public ResponseEntity<ApiResponse<Page<AdvanceDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(service.list(tenantId, pageable)));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('EXPENSE:READ')")
    public ResponseEntity<ApiResponse<List<AdvanceDto.Response>>> listByEmployee(@PathVariable UUID employeeId) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(service.listByEmployee(tenantId, employeeId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EXPENSE:WRITE')")
    public ResponseEntity<ApiResponse<AdvanceDto.Response>> update(
            @PathVariable UUID id,
            @RequestBody AdvanceDto.UpdateRequest request) {
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

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('EXPENSE:WRITE')")
    public ResponseEntity<ApiResponse<AdvanceDto.Response>> approve(
            @PathVariable UUID id,
            @RequestBody Map<String, UUID> body) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        UUID approvedBy = body.get("approvedBy");
        return ResponseEntity.ok(ApiResponse.ok(service.approve(tenantId, id, approvedBy, currentUser)));
    }
}
