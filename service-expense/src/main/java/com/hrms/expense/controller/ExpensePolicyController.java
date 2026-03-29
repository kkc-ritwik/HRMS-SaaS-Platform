package com.hrms.expense.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.expense.dto.ExpensePolicyDto;
import com.hrms.expense.service.ExpensePolicyService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expenses/policies")
@RequiredArgsConstructor
public class ExpensePolicyController {

    private final ExpensePolicyService service;

    @PostMapping
    @PreAuthorize("hasAuthority('EXPENSE:WRITE')")
    public ResponseEntity<ApiResponse<ExpensePolicyDto.Response>> create(
            @Valid @RequestBody ExpensePolicyDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        ExpensePolicyDto.Response response = service.create(tenantId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EXPENSE:READ')")
    public ResponseEntity<ApiResponse<ExpensePolicyDto.Response>> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(service.getById(tenantId, id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EXPENSE:READ')")
    public ResponseEntity<ApiResponse<Page<ExpensePolicyDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(service.list(tenantId, pageable)));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('EXPENSE:READ')")
    public ResponseEntity<ApiResponse<List<ExpensePolicyDto.Response>>> listAll() {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(service.listAll(tenantId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EXPENSE:WRITE')")
    public ResponseEntity<ApiResponse<ExpensePolicyDto.Response>> update(
            @PathVariable UUID id,
            @RequestBody ExpensePolicyDto.UpdateRequest request) {
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
}
