package com.hrms.reports.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.reports.dto.SavedReportDto;
import com.hrms.reports.service.SavedReportService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports/saved")
@RequiredArgsConstructor
public class SavedReportController {

    private final SavedReportService savedReportService;

    @PostMapping
    @PreAuthorize("hasAuthority('REPORTS:WRITE')")
    public ResponseEntity<ApiResponse<SavedReportDto.Response>> create(
            @Valid @RequestBody SavedReportDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(savedReportService.create(tenantId, request, currentUser)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORTS:READ')")
    public ResponseEntity<ApiResponse<SavedReportDto.Response>> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(savedReportService.getById(tenantId, id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REPORTS:READ')")
    public ResponseEntity<ApiResponse<Page<SavedReportDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(savedReportService.list(tenantId, pageable)));
    }

    @GetMapping("/definition/{definitionId}")
    @PreAuthorize("hasAuthority('REPORTS:READ')")
    public ResponseEntity<ApiResponse<Page<SavedReportDto.Response>>> listByDefinition(
            @PathVariable UUID definitionId,
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(savedReportService.listByDefinition(tenantId, definitionId, pageable)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('REPORTS:READ')")
    public ResponseEntity<ApiResponse<Page<SavedReportDto.Response>>> listMine(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID currentUserId = UUID.fromString(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(savedReportService.listByUser(tenantId, currentUserId, pageable)));
    }

    @GetMapping("/user/{generatedBy}")
    @PreAuthorize("hasAuthority('REPORTS:READ')")
    public ResponseEntity<ApiResponse<Page<SavedReportDto.Response>>> listByUser(
            @PathVariable UUID generatedBy,
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(savedReportService.listByUser(tenantId, generatedBy, pageable)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORTS:WRITE')")
    public ResponseEntity<ApiResponse<SavedReportDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody SavedReportDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(savedReportService.update(tenantId, id, request, currentUser)));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('REPORTS:WRITE')")
    public ResponseEntity<ApiResponse<SavedReportDto.Response>> markCompleted(
            @PathVariable UUID id,
            @RequestBody String fileUrl) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(savedReportService.markCompleted(tenantId, id, fileUrl, currentUser)));
    }

    @PostMapping("/{id}/fail")
    @PreAuthorize("hasAuthority('REPORTS:WRITE')")
    public ResponseEntity<ApiResponse<SavedReportDto.Response>> markFailed(
            @PathVariable UUID id,
            @RequestBody String error) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(savedReportService.markFailed(tenantId, id, error, currentUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORTS:WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        savedReportService.delete(tenantId, id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
