package com.hrms.asset.controller;

import com.hrms.asset.dto.AssetDto;
import com.hrms.asset.entity.Asset.AssetStatus;
import com.hrms.asset.service.AssetService;
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
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@Tag(name = "Assets", description = "Manage assets")
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Create an asset")
    public ResponseEntity<ApiResponse<AssetDto.Response>> create(
            @Valid @RequestBody AssetDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(assetService.create(tenantId(), req, currentUserId())));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "List assets (paginated)")
    public ResponseEntity<ApiResponse<List<AssetDto.Response>>> list(
            @PageableDefault(size = 50) Pageable pageable) {
        Page<AssetDto.Response> page = assetService.list(tenantId(), pageable);
        PaginationMeta meta = assetService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "List all assets")
    public ResponseEntity<ApiResponse<List<AssetDto.Response>>> listAll() {
        return ResponseEntity.ok(ApiResponse.ok(assetService.listAll(tenantId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "Get an asset by ID")
    public ResponseEntity<ApiResponse<AssetDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(assetService.getById(tenantId(), id)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "List assets by status")
    public ResponseEntity<ApiResponse<List<AssetDto.Response>>> listByStatus(
            @PathVariable AssetStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(assetService.listByStatus(tenantId(), status)));
    }

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "List assets by category")
    public ResponseEntity<ApiResponse<List<AssetDto.Response>>> listByCategory(
            @PathVariable UUID categoryId) {
        return ResponseEntity.ok(ApiResponse.ok(assetService.listByCategory(tenantId(), categoryId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Update an asset")
    public ResponseEntity<ApiResponse<AssetDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody AssetDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                assetService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Soft-delete an asset")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        assetService.delete(tenantId(), id, currentUserId());
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
