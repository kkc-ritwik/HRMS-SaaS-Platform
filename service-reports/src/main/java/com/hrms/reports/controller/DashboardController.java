package com.hrms.reports.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.reports.dto.DashboardDto;
import com.hrms.reports.service.DashboardService;
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
@RequestMapping("/api/v1/reports/dashboards")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @PostMapping
    @PreAuthorize("hasAuthority('REPORTS:WRITE')")
    public ResponseEntity<ApiResponse<DashboardDto.Response>> create(
            @Valid @RequestBody DashboardDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dashboardService.create(tenantId, request, currentUser)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORTS:READ')")
    public ResponseEntity<ApiResponse<DashboardDto.Response>> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getById(tenantId, id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REPORTS:READ')")
    public ResponseEntity<ApiResponse<Page<DashboardDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.list(tenantId, pageable)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('REPORTS:READ')")
    public ResponseEntity<ApiResponse<List<DashboardDto.Response>>> listMine() {
        String tenantId = TenantContext.get();
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID ownerId = UUID.fromString(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.listByOwner(tenantId, ownerId)));
    }

    @GetMapping("/shared")
    @PreAuthorize("hasAuthority('REPORTS:READ')")
    public ResponseEntity<ApiResponse<Page<DashboardDto.Response>>> listShared(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.listShared(tenantId, pageable)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORTS:WRITE')")
    public ResponseEntity<ApiResponse<DashboardDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DashboardDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.update(tenantId, id, request, currentUser)));
    }

    @PostMapping("/{id}/set-default")
    @PreAuthorize("hasAuthority('REPORTS:WRITE')")
    public ResponseEntity<ApiResponse<DashboardDto.Response>> setDefault(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUser = principal.getId();
        UUID ownerId = UUID.fromString(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.setDefault(tenantId, id, ownerId, currentUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORTS:WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        dashboardService.delete(tenantId, id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
