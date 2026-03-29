package com.hrms.performance.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.performance.dto.ReviewDto;
import com.hrms.performance.service.ReviewService;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "360-degree review assignment, submission, acknowledgement and calibration")
public class ReviewController {

    private final ReviewService reviewService;

    // ── Assignment ────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAuthority('PERFORMANCE:ADMIN')")
    @Operation(summary = "Assign a review (HR/admin creates self/manager/peer review assignments)")
    public ResponseEntity<ApiResponse<ReviewDto.Response>> create(
            @Valid @RequestBody ReviewDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(reviewService.create(tenantId(), req, currentUserId())));
    }

    // ── Submit / Save Draft ───────────────────────────────────────────────────

    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('PERFORMANCE:WRITE')")
    @Operation(summary = "Save draft or submit a review; set submit=true to submit")
    public ResponseEntity<ApiResponse<ReviewDto.Response>> saveOrSubmit(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewDto.SubmitRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                reviewService.saveOrSubmit(tenantId(), id, req, currentUserId())));
    }

    // ── Employee acknowledges manager review ──────────────────────────────────

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasAuthority('PERFORMANCE:WRITE')")
    @Operation(summary = "Employee acknowledges a SUBMITTED manager review")
    public ResponseEntity<ApiResponse<ReviewDto.Response>> acknowledge(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                reviewService.acknowledge(tenantId(), id, currentUserId())));
    }

    // ── Calibration ───────────────────────────────────────────────────────────

    @PostMapping("/{id}/calibrate")
    @PreAuthorize("hasAuthority('PERFORMANCE:ADMIN')")
    @Operation(summary = "HR calibrates and finalizes a review (adjusts ratings)")
    public ResponseEntity<ApiResponse<ReviewDto.Response>> calibrate(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewDto.CalibrateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                reviewService.calibrate(tenantId(), id, req, currentUserId())));
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "Get a review by ID (includes individual ratings)")
    public ResponseEntity<ApiResponse<ReviewDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.get(tenantId(), id)));
    }

    @GetMapping("/cycle/{cycleId}")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "List all reviews in a cycle")
    public ResponseEntity<ApiResponse<List<ReviewDto.Response>>> listByCycle(
            @PathVariable UUID cycleId) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.listByCycle(tenantId(), cycleId)));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "List all reviews for an employee")
    public ResponseEntity<ApiResponse<List<ReviewDto.Response>>> listForEmployee(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.listForEmployee(tenantId(), employeeId)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "List reviews where I am the subject employee")
    public ResponseEntity<ApiResponse<List<ReviewDto.Response>>> myReviews() {
        UUID employeeId = UUID.fromString(principal().getEmployeeId());
        return ResponseEntity.ok(ApiResponse.ok(reviewService.listForEmployee(tenantId(), employeeId)));
    }

    @GetMapping("/me/pending")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "List reviews assigned to me as reviewer that are still DRAFT")
    public ResponseEntity<ApiResponse<List<ReviewDto.Response>>> myPendingReviews() {
        UUID reviewerId = UUID.fromString(principal().getEmployeeId());
        return ResponseEntity.ok(ApiResponse.ok(reviewService.myPendingReviews(tenantId(), reviewerId)));
    }

    @GetMapping("/cycle/{cycleId}/team/{managerId}")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "List manager reviews for a manager's team in a cycle")
    public ResponseEntity<ApiResponse<List<ReviewDto.Response>>> teamReviews(
            @PathVariable UUID cycleId,
            @PathVariable UUID managerId) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.teamReviews(tenantId(), cycleId, managerId)));
    }

    private String tenantId() { return TenantContext.get(); }

    private UserPrincipal principal() {
        return (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    private String currentUserId() { return principal().getId(); }
}
