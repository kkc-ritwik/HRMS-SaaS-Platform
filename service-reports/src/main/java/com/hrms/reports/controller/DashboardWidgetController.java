package com.hrms.reports.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.reports.dto.DashboardWidgetDto;
import com.hrms.reports.service.DashboardWidgetService;
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
@RequestMapping("/api/v1/reports/widgets")
@RequiredArgsConstructor
public class DashboardWidgetController {

    private final DashboardWidgetService dashboardWidgetService;

    @PostMapping
    @PreAuthorize("hasAuthority('REPORTS:WRITE')")
    public ResponseEntity<ApiResponse<DashboardWidgetDto.Response>> create(
            @Valid @RequestBody DashboardWidgetDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dashboardWidgetService.create(tenantId, request, currentUser)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORTS:READ')")
    public ResponseEntity<ApiResponse<DashboardWidgetDto.Response>> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(dashboardWidgetService.getById(tenantId, id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REPORTS:READ')")
    public ResponseEntity<ApiResponse<Page<DashboardWidgetDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(dashboardWidgetService.list(tenantId, pageable)));
    }

    @GetMapping("/dashboard/{dashboardId}")
    @PreAuthorize("hasAuthority('REPORTS:READ')")
    public ResponseEntity<ApiResponse<List<DashboardWidgetDto.Response>>> listByDashboard(
            @PathVariable UUID dashboardId) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(dashboardWidgetService.listByDashboard(tenantId, dashboardId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORTS:WRITE')")
    public ResponseEntity<ApiResponse<DashboardWidgetDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DashboardWidgetDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(dashboardWidgetService.update(tenantId, id, request, currentUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORTS:WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        dashboardWidgetService.delete(tenantId, id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
