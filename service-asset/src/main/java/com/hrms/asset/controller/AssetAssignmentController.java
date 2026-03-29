package com.hrms.asset.controller;

import com.hrms.asset.dto.AssetAssignmentDto;
import com.hrms.asset.service.AssetAssignmentService;
import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assets/assignments")
@RequiredArgsConstructor
@Tag(name = "Asset Assignments", description = "Manage asset assignments to employees")
public class AssetAssignmentController {

    private final AssetAssignmentService assetAssignmentService;

    @PostMapping
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Create an asset assignment")
    public ResponseEntity<ApiResponse<AssetAssignmentDto.Response>> create(
            @Valid @RequestBody AssetAssignmentDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(assetAssignmentService.create(tenantId(), req, currentUserId())));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "List asset assignments (paginated)")
    public ResponseEntity<ApiResponse<List<AssetAssignmentDto.Response>>> list(
            @PageableDefault(size = 50) Pageable pageable) {
        Page<AssetAssignmentDto.Response> page = assetAssignmentService.list(tenantId(), pageable);
        PaginationMeta meta = assetAssignmentService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "Get an asset assignment by ID")
    public ResponseEntity<ApiResponse<AssetAssignmentDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(assetAssignmentService.getById(tenantId(), id)));
    }

    @GetMapping("/asset/{assetId}")
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "List assignments for a specific asset")
    public ResponseEntity<ApiResponse<List<AssetAssignmentDto.Response>>> listByAsset(
            @PathVariable UUID assetId) {
        return ResponseEntity.ok(ApiResponse.ok(
                assetAssignmentService.listByAsset(tenantId(), assetId)));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "List assignments for a specific employee")
    public ResponseEntity<ApiResponse<List<AssetAssignmentDto.Response>>> listByEmployee(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(
                assetAssignmentService.listByEmployee(tenantId(), employeeId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Update an asset assignment")
    public ResponseEntity<ApiResponse<AssetAssignmentDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody AssetAssignmentDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                assetAssignmentService.update(tenantId(), id, req, currentUserId())));
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Return an assigned asset")
    public ResponseEntity<ApiResponse<AssetAssignmentDto.Response>> returnAsset(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        String conditionAtReturn = (body != null) ? body.get("conditionAtReturn") : null;
        return ResponseEntity.ok(ApiResponse.ok(
                assetAssignmentService.returnAsset(tenantId(), id, conditionAtReturn, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Soft-delete an asset assignment")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        assetAssignmentService.delete(tenantId(), id, currentUserId());
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
