package com.hrms.compensation.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.compensation.dto.CompensationPlanDto;
import com.hrms.compensation.service.CompensationPlanService;
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
@RequestMapping("/api/v1/compensation/plans")
@RequiredArgsConstructor
public class CompensationPlanController {

    private final CompensationPlanService compensationPlanService;

    @PostMapping
    @PreAuthorize("hasAuthority('COMPENSATION:WRITE')")
    public ResponseEntity<ApiResponse<CompensationPlanDto.Response>> create(
            @Valid @RequestBody CompensationPlanDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        CompensationPlanDto.Response response = compensationPlanService.create(tenantId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPENSATION:READ')")
    public ResponseEntity<ApiResponse<CompensationPlanDto.Response>> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(compensationPlanService.getById(tenantId, id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COMPENSATION:READ')")
    public ResponseEntity<ApiResponse<Page<CompensationPlanDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(compensationPlanService.list(tenantId, pageable)));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('COMPENSATION:READ')")
    public ResponseEntity<ApiResponse<List<CompensationPlanDto.Response>>> listByEmployee(
            @PathVariable UUID employeeId) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(compensationPlanService.listByEmployee(tenantId, employeeId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPENSATION:WRITE')")
    public ResponseEntity<ApiResponse<CompensationPlanDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CompensationPlanDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(compensationPlanService.update(tenantId, id, request, currentUser)));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('COMPENSATION:WRITE')")
    public ResponseEntity<ApiResponse<CompensationPlanDto.Response>> activate(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(compensationPlanService.activate(tenantId, id, currentUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPENSATION:WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        compensationPlanService.delete(tenantId, id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
