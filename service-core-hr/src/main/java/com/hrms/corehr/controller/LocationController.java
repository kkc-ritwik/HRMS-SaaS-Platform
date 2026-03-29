package com.hrms.corehr.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.corehr.dto.LocationDto;
import com.hrms.corehr.service.LocationService;
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
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Tag(name = "Locations", description = "Location / office management")
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEES:WRITE')")
    @Operation(summary = "Create a location")
    public ResponseEntity<ApiResponse<LocationDto.Response>> create(
            @Valid @RequestBody LocationDto.CreateRequest req) {

        LocationDto.Response response = locationService.create(tenantId(), req, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    @Operation(summary = "List locations with optional search and pagination")
    public ResponseEntity<ApiResponse<List<LocationDto.Response>>> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<LocationDto.Response> page = locationService.list(tenantId(), search, pageable);
        PaginationMeta meta = locationService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/active")
    @Operation(summary = "List all active locations (no pagination)")
    public ResponseEntity<ApiResponse<List<LocationDto.Response>>> listActive() {
        return ResponseEntity.ok(ApiResponse.ok(locationService.listActive(tenantId())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a location by ID")
    public ResponseEntity<ApiResponse<LocationDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(locationService.getById(tenantId(), id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEES:WRITE')")
    @Operation(summary = "Update a location")
    public ResponseEntity<ApiResponse<LocationDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody LocationDto.UpdateRequest req) {

        return ResponseEntity.ok(ApiResponse.ok(
                locationService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEES:DELETE')")
    @Operation(summary = "Soft-delete a location")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        locationService.delete(tenantId(), id, currentUserId());
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
