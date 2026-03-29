package com.hrms.performance.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.performance.dto.PipPlanDto;
import com.hrms.performance.service.PipPlanService;
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
@RequestMapping("/api/v1/pip-plans")
@RequiredArgsConstructor
@Tag(name = "PIP Plans", description = "Performance Improvement Plans with structured improvement areas")
public class PipPlanController {

    private final PipPlanService pipPlanService;

    @PostMapping
    @PreAuthorize("hasAuthority('PERFORMANCE:ADMIN')")
    @Operation(summary = "Create a new PIP plan (starts as DRAFT)")
    public ResponseEntity<ApiResponse<PipPlanDto.Response>> create(
            @Valid @RequestBody PipPlanDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(pipPlanService.create(tenantId(), req, currentUserId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFORMANCE:ADMIN')")
    @Operation(summary = "Update a PIP plan (cannot update terminal-status plans)")
    public ResponseEntity<ApiResponse<PipPlanDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody PipPlanDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                pipPlanService.update(tenantId(), id, req, currentUserId())));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('PERFORMANCE:ADMIN')")
    @Operation(summary = "Activate a DRAFT PIP plan (transitions to ACTIVE)")
    public ResponseEntity<ApiResponse<PipPlanDto.Response>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                pipPlanService.activate(tenantId(), id, currentUserId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "Get a PIP plan by ID")
    public ResponseEntity<ApiResponse<PipPlanDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(pipPlanService.get(tenantId(), id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERFORMANCE:ADMIN')")
    @Operation(summary = "List all PIP plans (paginated, HR/admin view)")
    public ResponseEntity<ApiResponse<List<PipPlanDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PipPlanDto.Response> page = pipPlanService.list(tenantId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), pipPlanService.buildMeta(page)));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "List all PIP plans for a specific employee")
    public ResponseEntity<ApiResponse<List<PipPlanDto.Response>>> listForEmployee(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(
                pipPlanService.listForEmployee(tenantId(), employeeId)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "List my own PIP plans (employee self-service)")
    public ResponseEntity<ApiResponse<List<PipPlanDto.Response>>> myPlans() {
        UUID employeeId = UUID.fromString(principal().getEmployeeId());
        return ResponseEntity.ok(ApiResponse.ok(pipPlanService.listForEmployee(tenantId(), employeeId)));
    }

    private String tenantId() { return TenantContext.get(); }

    private UserPrincipal principal() {
        return (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    private String currentUserId() { return principal().getId(); }
}
