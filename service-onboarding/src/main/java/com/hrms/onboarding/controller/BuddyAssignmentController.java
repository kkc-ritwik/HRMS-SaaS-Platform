package com.hrms.onboarding.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.onboarding.dto.BuddyAssignmentDto;
import com.hrms.onboarding.service.BuddyAssignmentService;
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
@RequestMapping("/api/v1/onboarding/buddy-assignments")
@RequiredArgsConstructor
@Tag(name = "Buddy Assignments", description = "Manage buddy assignments for onboarding")
public class BuddyAssignmentController {

    private final BuddyAssignmentService buddyAssignmentService;

    @PostMapping
    @PreAuthorize("hasAuthority('ONBOARDING:WRITE')")
    @Operation(summary = "Create a buddy assignment")
    public ResponseEntity<ApiResponse<BuddyAssignmentDto.Response>> create(
            @Valid @RequestBody BuddyAssignmentDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(buddyAssignmentService.create(tenantId(), req, currentUserId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ONBOARDING:READ')")
    @Operation(summary = "Get a buddy assignment by ID")
    public ResponseEntity<ApiResponse<BuddyAssignmentDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(buddyAssignmentService.get(tenantId(), id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ONBOARDING:READ')")
    @Operation(summary = "List buddy assignments with pagination")
    public ResponseEntity<ApiResponse<List<BuddyAssignmentDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<BuddyAssignmentDto.Response> page = buddyAssignmentService.list(tenantId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), buddyAssignmentService.buildMeta(page)));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('ONBOARDING:READ')")
    @Operation(summary = "List buddy assignments by employee")
    public ResponseEntity<ApiResponse<List<BuddyAssignmentDto.Response>>> listByEmployee(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(buddyAssignmentService.listByEmployee(tenantId(), employeeId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ONBOARDING:WRITE')")
    @Operation(summary = "Update a buddy assignment")
    public ResponseEntity<ApiResponse<BuddyAssignmentDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody BuddyAssignmentDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(buddyAssignmentService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ONBOARDING:WRITE')")
    @Operation(summary = "Delete a buddy assignment")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        buddyAssignmentService.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    private String tenantId() {
        return TenantContext.get();
    }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}
