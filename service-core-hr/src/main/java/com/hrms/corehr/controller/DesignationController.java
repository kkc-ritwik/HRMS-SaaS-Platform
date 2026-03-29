package com.hrms.corehr.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.corehr.dto.DesignationDto;
import com.hrms.corehr.service.DesignationService;
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
@RequestMapping("/api/v1/designations")
@RequiredArgsConstructor
@Tag(name = "Designations", description = "Designation management")
public class DesignationController {

    private final DesignationService designationService;

    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEES:WRITE')")
    @Operation(summary = "Create a designation")
    public ResponseEntity<ApiResponse<DesignationDto.Response>> create(
            @Valid @RequestBody DesignationDto.CreateRequest req) {

        DesignationDto.Response response = designationService.create(
                tenantId(), req, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    @Operation(summary = "List designations with optional search and pagination")
    public ResponseEntity<ApiResponse<List<DesignationDto.Response>>> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<DesignationDto.Response> page = designationService.list(tenantId(), search, pageable);
        PaginationMeta meta = designationService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/active")
    @Operation(summary = "List all active designations (no pagination)")
    public ResponseEntity<ApiResponse<List<DesignationDto.Response>>> listActive() {
        return ResponseEntity.ok(ApiResponse.ok(designationService.listActive(tenantId())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a designation by ID")
    public ResponseEntity<ApiResponse<DesignationDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(designationService.getById(tenantId(), id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEES:WRITE')")
    @Operation(summary = "Update a designation")
    public ResponseEntity<ApiResponse<DesignationDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DesignationDto.UpdateRequest req) {

        return ResponseEntity.ok(ApiResponse.ok(
                designationService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEES:DELETE')")
    @Operation(summary = "Soft-delete a designation")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        designationService.delete(tenantId(), id, currentUserId());
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
