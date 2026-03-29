package com.hrms.compliance.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.compliance.dto.ComplianceTaskDto;
import com.hrms.compliance.service.ComplianceTaskService;
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
@RequestMapping("/api/v1/compliance/tasks")
@RequiredArgsConstructor
@Tag(name = "Compliance Tasks", description = "Compliance task management")
public class ComplianceTaskController {

    private final ComplianceTaskService service;

    @PostMapping
    @PreAuthorize("hasAuthority('COMPLIANCE:WRITE')")
    @Operation(summary = "Create a compliance task")
    public ResponseEntity<ApiResponse<ComplianceTaskDto.Response>> create(
            @Valid @RequestBody ComplianceTaskDto.CreateRequest request) {
        ComplianceTaskDto.Response response = service.create(tenantId(), request, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COMPLIANCE:READ')")
    @Operation(summary = "List compliance tasks with pagination")
    public ResponseEntity<ApiResponse<List<ComplianceTaskDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ComplianceTaskDto.Response> page = service.list(tenantId(), pageable);
        PaginationMeta meta = PaginationMeta.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPLIANCE:READ')")
    @Operation(summary = "Get a compliance task by ID")
    public ResponseEntity<ApiResponse<ComplianceTaskDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.getById(tenantId(), id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPLIANCE:WRITE')")
    @Operation(summary = "Update a compliance task")
    public ResponseEntity<ApiResponse<ComplianceTaskDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ComplianceTaskDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(service.update(tenantId(), id, request, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPLIANCE:WRITE')")
    @Operation(summary = "Soft-delete a compliance task")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/item/{complianceItemId}")
    @PreAuthorize("hasAuthority('COMPLIANCE:READ')")
    @Operation(summary = "List tasks for a specific compliance item")
    public ResponseEntity<ApiResponse<List<ComplianceTaskDto.Response>>> listByItem(
            @PathVariable UUID complianceItemId) {
        return ResponseEntity.ok(ApiResponse.ok(service.listByItem(tenantId(), complianceItemId)));
    }

    @GetMapping("/assignee/{assigneeId}")
    @PreAuthorize("hasAuthority('COMPLIANCE:READ')")
    @Operation(summary = "List tasks assigned to a specific user")
    public ResponseEntity<ApiResponse<List<ComplianceTaskDto.Response>>> listByAssignee(
            @PathVariable UUID assigneeId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ComplianceTaskDto.Response> page = service.listByAssignee(tenantId(), assigneeId, pageable);
        PaginationMeta meta = PaginationMeta.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('COMPLIANCE:WRITE')")
    @Operation(summary = "Mark a compliance task as completed")
    public ResponseEntity<ApiResponse<ComplianceTaskDto.Response>> complete(
            @PathVariable UUID id,
            @RequestParam(required = false) String evidenceUrl) {
        return ResponseEntity.ok(ApiResponse.ok(service.complete(tenantId(), id, evidenceUrl, currentUserId())));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String tenantId() {
        return TenantContext.get();
    }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
