package com.hrms.asset.controller;

import com.hrms.asset.dto.AssetMaintenanceDto;
import com.hrms.asset.service.AssetMaintenanceService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assets/maintenance")
@RequiredArgsConstructor
@Tag(name = "Asset Maintenance", description = "Manage asset maintenance records")
public class AssetMaintenanceController {

    private final AssetMaintenanceService assetMaintenanceService;

    @PostMapping
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Create an asset maintenance record")
    public ResponseEntity<ApiResponse<AssetMaintenanceDto.Response>> create(
            @Valid @RequestBody AssetMaintenanceDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(assetMaintenanceService.create(tenantId(), req, currentUserId())));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "List maintenance records (paginated)")
    public ResponseEntity<ApiResponse<List<AssetMaintenanceDto.Response>>> list(
            @PageableDefault(size = 50) Pageable pageable) {
        Page<AssetMaintenanceDto.Response> page = assetMaintenanceService.list(tenantId(), pageable);
        PaginationMeta meta = assetMaintenanceService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "Get a maintenance record by ID")
    public ResponseEntity<ApiResponse<AssetMaintenanceDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(assetMaintenanceService.getById(tenantId(), id)));
    }

    @GetMapping("/asset/{assetId}")
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "List maintenance records for a specific asset")
    public ResponseEntity<ApiResponse<List<AssetMaintenanceDto.Response>>> listByAsset(
            @PathVariable UUID assetId) {
        return ResponseEntity.ok(ApiResponse.ok(
                assetMaintenanceService.listByAsset(tenantId(), assetId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Update a maintenance record")
    public ResponseEntity<ApiResponse<AssetMaintenanceDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody AssetMaintenanceDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                assetMaintenanceService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Soft-delete a maintenance record")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        assetMaintenanceService.delete(tenantId(), id, currentUserId());
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
