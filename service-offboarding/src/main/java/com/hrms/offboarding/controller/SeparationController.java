package com.hrms.offboarding.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.offboarding.dto.SeparationDto;
import com.hrms.offboarding.entity.Separation.SeparationStatus;
import com.hrms.offboarding.service.SeparationService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/offboarding/separations")
@RequiredArgsConstructor
@Tag(name = "Separations", description = "Manage employee separations")
public class SeparationController {

    private final SeparationService separationService;

    @PostMapping
    @PreAuthorize("hasAuthority('OFFBOARDING:WRITE')")
    @Operation(summary = "Create a separation record")
    public ResponseEntity<ApiResponse<SeparationDto.Response>> create(
            @Valid @RequestBody SeparationDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(separationService.create(tenantId(), req, currentUserId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('OFFBOARDING:READ')")
    @Operation(summary = "Get a separation by ID")
    public ResponseEntity<ApiResponse<SeparationDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(separationService.get(tenantId(), id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('OFFBOARDING:READ')")
    @Operation(summary = "List all separations with pagination")
    public ResponseEntity<ApiResponse<List<SeparationDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SeparationDto.Response> page = separationService.list(tenantId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), separationService.buildMeta(page)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('OFFBOARDING:WRITE')")
    @Operation(summary = "Update a separation record")
    public ResponseEntity<ApiResponse<SeparationDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody SeparationDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(separationService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('OFFBOARDING:WRITE')")
    @Operation(summary = "Soft-delete a separation record")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        separationService.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('OFFBOARDING:READ')")
    @Operation(summary = "List separations by employee")
    public ResponseEntity<ApiResponse<List<SeparationDto.Response>>> listByEmployee(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(separationService.listByEmployee(tenantId(), employeeId)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('OFFBOARDING:READ')")
    @Operation(summary = "List separations by status with pagination")
    public ResponseEntity<ApiResponse<List<SeparationDto.Response>>> listByStatus(
            @PathVariable SeparationStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SeparationDto.Response> page = separationService.listByStatus(tenantId(), status, pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), separationService.buildMeta(page)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('OFFBOARDING:WRITE')")
    @Operation(summary = "Approve a separation (moves status to IN_PROGRESS)")
    public ResponseEntity<ApiResponse<SeparationDto.Response>> approve(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        UUID approvedBy = UUID.fromString(body.get("approvedBy"));
        return ResponseEntity.ok(ApiResponse.ok(separationService.approve(tenantId(), id, approvedBy, currentUserId())));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('OFFBOARDING:WRITE')")
    @Operation(summary = "Complete a separation (moves status to COMPLETED)")
    public ResponseEntity<ApiResponse<SeparationDto.Response>> complete(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        LocalDate finalSettlementDate = body.containsKey("finalSettlementDate")
                ? LocalDate.parse(body.get("finalSettlementDate"))
                : null;
        return ResponseEntity.ok(ApiResponse.ok(separationService.complete(tenantId(), id, finalSettlementDate, currentUserId())));
    }

    private String tenantId() {
        return TenantContext.get();
    }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}
