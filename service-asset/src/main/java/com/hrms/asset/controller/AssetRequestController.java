package com.hrms.asset.controller;

import com.hrms.asset.dto.AssetRequestDto;
import com.hrms.asset.service.AssetRequestService;
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
@RequestMapping("/api/v1/assets/requests")
@RequiredArgsConstructor
@Tag(name = "Asset Requests", description = "Manage employee asset requests")
public class AssetRequestController {

    private final AssetRequestService assetRequestService;

    @PostMapping
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Create an asset request")
    public ResponseEntity<ApiResponse<AssetRequestDto.Response>> create(
            @Valid @RequestBody AssetRequestDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(assetRequestService.create(tenantId(), req, currentUserId())));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "List asset requests (paginated)")
    public ResponseEntity<ApiResponse<List<AssetRequestDto.Response>>> list(
            @PageableDefault(size = 50) Pageable pageable) {
        Page<AssetRequestDto.Response> page = assetRequestService.list(tenantId(), pageable);
        PaginationMeta meta = assetRequestService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "Get an asset request by ID")
    public ResponseEntity<ApiResponse<AssetRequestDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(assetRequestService.getById(tenantId(), id)));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "List asset requests for a specific employee")
    public ResponseEntity<ApiResponse<List<AssetRequestDto.Response>>> listByEmployee(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(
                assetRequestService.listByEmployee(tenantId(), employeeId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Update an asset request")
    public ResponseEntity<ApiResponse<AssetRequestDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody AssetRequestDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                assetRequestService.update(tenantId(), id, req, currentUserId())));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Approve an asset request")
    public ResponseEntity<ApiResponse<AssetRequestDto.Response>> approve(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        UUID approvedBy = (body != null && body.get("approvedBy") != null)
                ? UUID.fromString(body.get("approvedBy"))
                : UUID.fromString(currentUserId());
        return ResponseEntity.ok(ApiResponse.ok(
                assetRequestService.approve(tenantId(), id, approvedBy, currentUserId())));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Reject an asset request")
    public ResponseEntity<ApiResponse<AssetRequestDto.Response>> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                assetRequestService.reject(tenantId(), id, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Soft-delete an asset request")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        assetRequestService.delete(tenantId(), id, currentUserId());
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
