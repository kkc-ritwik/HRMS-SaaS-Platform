package com.hrms.performance.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.performance.dto.ReviewCycleDto;
import com.hrms.performance.entity.ReviewCycle;
import com.hrms.performance.service.ReviewCycleService;
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
@RequestMapping("/api/v1/performance/cycles")
@RequiredArgsConstructor
@Tag(name = "Review Cycles", description = "Manage performance review cycle lifecycle")
public class ReviewCycleController {

    private final ReviewCycleService cycleService;

    @PostMapping
    @PreAuthorize("hasAuthority('PERFORMANCE:ADMIN')")
    @Operation(summary = "Create a new review cycle (DRAFT)")
    public ResponseEntity<ApiResponse<ReviewCycleDto.Response>> create(
            @Valid @RequestBody ReviewCycleDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(cycleService.create(tenantId(), req, currentUserId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFORMANCE:ADMIN')")
    @Operation(summary = "Update a DRAFT review cycle")
    public ResponseEntity<ApiResponse<ReviewCycleDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewCycleDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                cycleService.update(tenantId(), id, req, currentUserId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "Get a review cycle by ID")
    public ResponseEntity<ApiResponse<ReviewCycleDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(cycleService.get(tenantId(), id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "List all review cycles (paginated, optional status filter)")
    public ResponseEntity<ApiResponse<List<ReviewCycleDto.Response>>> list(
            @RequestParam(required = false) ReviewCycle.CycleStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        if (status != null) {
            return ResponseEntity.ok(ApiResponse.ok(cycleService.listByStatus(tenantId(), status)));
        }
        Page<ReviewCycleDto.Response> page = cycleService.list(tenantId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), cycleService.buildMeta(page)));
    }

    // ── Workflow transitions ──────────────────────────────────────────────────

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('PERFORMANCE:ADMIN')")
    @Operation(summary = "Transition cycle DRAFT → ACTIVE")
    public ResponseEntity<ApiResponse<ReviewCycleDto.Response>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                cycleService.transition(tenantId(), id, ReviewCycle.CycleStatus.ACTIVE, currentUserId())));
    }

    @PostMapping("/{id}/start-self-review")
    @PreAuthorize("hasAuthority('PERFORMANCE:ADMIN')")
    @Operation(summary = "Transition cycle ACTIVE → SELF_REVIEW")
    public ResponseEntity<ApiResponse<ReviewCycleDto.Response>> startSelfReview(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                cycleService.transition(tenantId(), id, ReviewCycle.CycleStatus.SELF_REVIEW, currentUserId())));
    }

    @PostMapping("/{id}/start-manager-review")
    @PreAuthorize("hasAuthority('PERFORMANCE:ADMIN')")
    @Operation(summary = "Transition cycle SELF_REVIEW → MANAGER_REVIEW")
    public ResponseEntity<ApiResponse<ReviewCycleDto.Response>> startManagerReview(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                cycleService.transition(tenantId(), id, ReviewCycle.CycleStatus.MANAGER_REVIEW, currentUserId())));
    }

    @PostMapping("/{id}/start-calibration")
    @PreAuthorize("hasAuthority('PERFORMANCE:ADMIN')")
    @Operation(summary = "Transition cycle MANAGER_REVIEW → CALIBRATION")
    public ResponseEntity<ApiResponse<ReviewCycleDto.Response>> startCalibration(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                cycleService.transition(tenantId(), id, ReviewCycle.CycleStatus.CALIBRATION, currentUserId())));
    }

    @PostMapping("/{id}/finalize")
    @PreAuthorize("hasAuthority('PERFORMANCE:ADMIN')")
    @Operation(summary = "Transition cycle CALIBRATION → FINALIZED")
    public ResponseEntity<ApiResponse<ReviewCycleDto.Response>> finalize(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                cycleService.transition(tenantId(), id, ReviewCycle.CycleStatus.FINALIZED, currentUserId())));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('PERFORMANCE:ADMIN')")
    @Operation(summary = "Transition cycle FINALIZED → CLOSED")
    public ResponseEntity<ApiResponse<ReviewCycleDto.Response>> close(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                cycleService.transition(tenantId(), id, ReviewCycle.CycleStatus.CLOSED, currentUserId())));
    }

    private String tenantId() { return TenantContext.get(); }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
