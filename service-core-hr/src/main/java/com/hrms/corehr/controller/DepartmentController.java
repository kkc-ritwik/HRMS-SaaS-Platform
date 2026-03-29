package com.hrms.corehr.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.corehr.dto.DepartmentDto;
import com.hrms.corehr.service.DepartmentService;
import com.hrms.security.model.UserPrincipal;
import com.hrms.security.model.TenantContext;
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
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@Tag(name = "Departments", description = "Department management")
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEES:WRITE')")
    @Operation(summary = "Create a department")
    public ResponseEntity<ApiResponse<DepartmentDto.Response>> create(
            @Valid @RequestBody DepartmentDto.CreateRequest req) {

        DepartmentDto.Response response = departmentService.create(
                tenantId(), req, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    @Operation(summary = "List departments with optional search and pagination")
    public ResponseEntity<ApiResponse<List<DepartmentDto.Response>>> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<DepartmentDto.Response> page = departmentService.list(tenantId(), search, pageable);
        PaginationMeta meta = departmentService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/active")
    @Operation(summary = "List all active departments (no pagination)")
    public ResponseEntity<ApiResponse<List<DepartmentDto.ListItem>>> listActive() {
        return ResponseEntity.ok(ApiResponse.ok(departmentService.listActive(tenantId())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a department by ID")
    public ResponseEntity<ApiResponse<DepartmentDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(departmentService.getById(tenantId(), id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEES:WRITE')")
    @Operation(summary = "Update a department")
    public ResponseEntity<ApiResponse<DepartmentDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DepartmentDto.UpdateRequest req) {

        return ResponseEntity.ok(ApiResponse.ok(
                departmentService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEES:DELETE')")
    @Operation(summary = "Soft-delete a department")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        departmentService.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
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
