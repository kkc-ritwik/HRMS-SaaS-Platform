package com.hrms.reports.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.reports.dto.ReportDefinitionDto;
import com.hrms.reports.service.ReportDefinitionService;
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
@RequestMapping("/api/v1/reports/definitions")
@RequiredArgsConstructor
public class ReportDefinitionController {

    private final ReportDefinitionService reportDefinitionService;

    @PostMapping
    @PreAuthorize("hasAuthority('REPORTS:WRITE')")
    public ResponseEntity<ApiResponse<ReportDefinitionDto.Response>> create(
            @Valid @RequestBody ReportDefinitionDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        ReportDefinitionDto.Response response = reportDefinitionService.create(tenantId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORTS:READ')")
    public ResponseEntity<ApiResponse<ReportDefinitionDto.Response>> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(reportDefinitionService.getById(tenantId, id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REPORTS:READ')")
    public ResponseEntity<ApiResponse<Page<ReportDefinitionDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(reportDefinitionService.list(tenantId, pageable)));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('REPORTS:READ')")
    public ResponseEntity<ApiResponse<List<ReportDefinitionDto.Response>>> listActive() {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(reportDefinitionService.listActive(tenantId)));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAuthority('REPORTS:READ')")
    public ResponseEntity<ApiResponse<Page<ReportDefinitionDto.Response>>> listByCategory(
            @PathVariable String category,
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(reportDefinitionService.listByCategory(tenantId, category, pageable)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORTS:WRITE')")
    public ResponseEntity<ApiResponse<ReportDefinitionDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ReportDefinitionDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(reportDefinitionService.update(tenantId, id, request, currentUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORTS:WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        reportDefinitionService.delete(tenantId, id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
