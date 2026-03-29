package com.hrms.onboarding.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.onboarding.dto.ProbationReviewDto;
import com.hrms.onboarding.service.ProbationReviewService;
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
@RequestMapping("/api/v1/onboarding/probation-reviews")
@RequiredArgsConstructor
@Tag(name = "Probation Reviews", description = "Manage probation reviews")
public class ProbationReviewController {

    private final ProbationReviewService probationReviewService;

    @PostMapping
    @PreAuthorize("hasAuthority('ONBOARDING:WRITE')")
    @Operation(summary = "Create a probation review")
    public ResponseEntity<ApiResponse<ProbationReviewDto.Response>> create(
            @Valid @RequestBody ProbationReviewDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(probationReviewService.create(tenantId(), req, currentUserId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ONBOARDING:READ')")
    @Operation(summary = "Get a probation review by ID")
    public ResponseEntity<ApiResponse<ProbationReviewDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(probationReviewService.get(tenantId(), id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ONBOARDING:READ')")
    @Operation(summary = "List probation reviews with pagination")
    public ResponseEntity<ApiResponse<List<ProbationReviewDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProbationReviewDto.Response> page = probationReviewService.list(tenantId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), probationReviewService.buildMeta(page)));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('ONBOARDING:READ')")
    @Operation(summary = "List probation reviews by employee")
    public ResponseEntity<ApiResponse<List<ProbationReviewDto.Response>>> listByEmployee(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(probationReviewService.listByEmployee(tenantId(), employeeId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ONBOARDING:WRITE')")
    @Operation(summary = "Update a probation review")
    public ResponseEntity<ApiResponse<ProbationReviewDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProbationReviewDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(probationReviewService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ONBOARDING:WRITE')")
    @Operation(summary = "Delete a probation review")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        probationReviewService.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    private String tenantId() {
        return TenantContext.get();
    }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}
