package com.hrms.asset.controller;

import com.hrms.asset.dto.AssetCategoryDto;
import com.hrms.asset.service.AssetCategoryService;
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
@RequestMapping("/api/v1/assets/categories")
@RequiredArgsConstructor
@Tag(name = "Asset Categories", description = "Manage asset category definitions")
public class AssetCategoryController {

    private final AssetCategoryService assetCategoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Create an asset category")
    public ResponseEntity<ApiResponse<AssetCategoryDto.Response>> create(
            @Valid @RequestBody AssetCategoryDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(assetCategoryService.create(tenantId(), req, currentUserId())));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "List asset categories (paginated)")
    public ResponseEntity<ApiResponse<List<AssetCategoryDto.Response>>> list(
            @PageableDefault(size = 50) Pageable pageable) {
        Page<AssetCategoryDto.Response> page = assetCategoryService.list(tenantId(), pageable);
        PaginationMeta meta = assetCategoryService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "List all asset categories (for dropdowns)")
    public ResponseEntity<ApiResponse<List<AssetCategoryDto.Response>>> listAll() {
        return ResponseEntity.ok(ApiResponse.ok(assetCategoryService.listAll(tenantId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "Get an asset category by ID")
    public ResponseEntity<ApiResponse<AssetCategoryDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(assetCategoryService.getById(tenantId(), id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Update an asset category")
    public ResponseEntity<ApiResponse<AssetCategoryDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody AssetCategoryDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                assetCategoryService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Soft-delete an asset category")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        assetCategoryService.delete(tenantId(), id, currentUserId());
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
