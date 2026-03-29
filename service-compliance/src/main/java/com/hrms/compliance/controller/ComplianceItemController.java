package com.hrms.compliance.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.compliance.dto.ComplianceItemDto;
import com.hrms.compliance.entity.ComplianceItem.ComplianceStatus;
import com.hrms.compliance.service.ComplianceItemService;
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
@RequestMapping("/api/v1/compliance/items")
@RequiredArgsConstructor
@Tag(name = "Compliance Items", description = "Compliance item management")
public class ComplianceItemController {

    private final ComplianceItemService service;

    @PostMapping
    @PreAuthorize("hasAuthority('COMPLIANCE:WRITE')")
    @Operation(summary = "Create a compliance item")
    public ResponseEntity<ApiResponse<ComplianceItemDto.Response>> create(
            @Valid @RequestBody ComplianceItemDto.CreateRequest request) {
        ComplianceItemDto.Response response = service.create(tenantId(), request, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COMPLIANCE:READ')")
    @Operation(summary = "List compliance items with pagination")
    public ResponseEntity<ApiResponse<List<ComplianceItemDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ComplianceItemDto.Response> page = service.list(tenantId(), pageable);
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
    @Operation(summary = "Get a compliance item by ID")
    public ResponseEntity<ApiResponse<ComplianceItemDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.getById(tenantId(), id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPLIANCE:WRITE')")
    @Operation(summary = "Update a compliance item")
    public ResponseEntity<ApiResponse<ComplianceItemDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ComplianceItemDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(service.update(tenantId(), id, request, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPLIANCE:WRITE')")
    @Operation(summary = "Soft-delete a compliance item")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('COMPLIANCE:READ')")
    @Operation(summary = "List compliance items by status")
    public ResponseEntity<ApiResponse<List<ComplianceItemDto.Response>>> listByStatus(
            @PathVariable ComplianceStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ComplianceItemDto.Response> page = service.listByStatus(tenantId(), status, pageable);
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

    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasAuthority('COMPLIANCE:READ')")
    @Operation(summary = "List compliance items by owner")
    public ResponseEntity<ApiResponse<List<ComplianceItemDto.Response>>> listByOwner(
            @PathVariable UUID ownerId) {
        return ResponseEntity.ok(ApiResponse.ok(service.listByOwner(tenantId(), ownerId)));
    }

    @PostMapping("/{id}/mark-compliant")
    @PreAuthorize("hasAuthority('COMPLIANCE:WRITE')")
    @Operation(summary = "Mark a compliance item as compliant")
    public ResponseEntity<ApiResponse<ComplianceItemDto.Response>> markCompliant(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.markCompliant(tenantId(), id, currentUserId())));
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
