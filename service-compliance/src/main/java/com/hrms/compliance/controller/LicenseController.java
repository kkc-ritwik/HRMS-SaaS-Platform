package com.hrms.compliance.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.compliance.dto.LicenseDto;
import com.hrms.compliance.entity.License.LicenseStatus;
import com.hrms.compliance.service.LicenseService;
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
@RequestMapping("/api/v1/compliance/licenses")
@RequiredArgsConstructor
@Tag(name = "Licenses", description = "Employee license management")
public class LicenseController {

    private final LicenseService service;

    @PostMapping
    @PreAuthorize("hasAuthority('COMPLIANCE:WRITE')")
    @Operation(summary = "Create a license record")
    public ResponseEntity<ApiResponse<LicenseDto.Response>> create(
            @Valid @RequestBody LicenseDto.CreateRequest request) {
        LicenseDto.Response response = service.create(tenantId(), request, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COMPLIANCE:READ')")
    @Operation(summary = "List licenses with pagination")
    public ResponseEntity<ApiResponse<List<LicenseDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<LicenseDto.Response> page = service.list(tenantId(), pageable);
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
    @Operation(summary = "Get a license by ID")
    public ResponseEntity<ApiResponse<LicenseDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.getById(tenantId(), id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPLIANCE:WRITE')")
    @Operation(summary = "Update a license record")
    public ResponseEntity<ApiResponse<LicenseDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody LicenseDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(service.update(tenantId(), id, request, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPLIANCE:WRITE')")
    @Operation(summary = "Soft-delete a license record")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('COMPLIANCE:READ')")
    @Operation(summary = "List licenses for a specific employee")
    public ResponseEntity<ApiResponse<List<LicenseDto.Response>>> listByEmployee(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(service.listByEmployee(tenantId(), employeeId)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('COMPLIANCE:READ')")
    @Operation(summary = "List licenses by status")
    public ResponseEntity<ApiResponse<List<LicenseDto.Response>>> listByStatus(
            @PathVariable LicenseStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(service.listByStatus(tenantId(), status)));
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
