package com.hrms.performance.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.performance.dto.GoalDto;
import com.hrms.performance.service.GoalService;
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
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
@Tag(name = "Goals / OKRs", description = "OKR goal management with Key Result progress tracking")
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    @PreAuthorize("hasAuthority('PERFORMANCE:WRITE')")
    @Operation(summary = "Create a new goal / OKR (Objective or Key Result)")
    public ResponseEntity<ApiResponse<GoalDto.Response>> create(
            @Valid @RequestBody GoalDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(goalService.create(tenantId(), req, currentUserId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFORMANCE:WRITE')")
    @Operation(summary = "Update a goal")
    public ResponseEntity<ApiResponse<GoalDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody GoalDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                goalService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFORMANCE:WRITE')")
    @Operation(summary = "Soft-delete a goal")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        goalService.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "Get a goal by ID (OBJECTIVE includes nested Key Results)")
    public ResponseEntity<ApiResponse<GoalDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(goalService.get(tenantId(), id)));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "List goals for an employee (paginated)")
    public ResponseEntity<ApiResponse<List<GoalDto.Response>>> listForEmployee(
            @PathVariable UUID employeeId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GoalDto.Response> page = goalService.listForEmployee(tenantId(), employeeId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), goalService.buildMeta(page)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "List my goals (self-service)")
    public ResponseEntity<ApiResponse<List<GoalDto.Response>>> myGoals(
            @PageableDefault(size = 20) Pageable pageable) {
        UUID employeeId = UUID.fromString(principal().getEmployeeId());
        Page<GoalDto.Response> page = goalService.listForEmployee(tenantId(), employeeId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), goalService.buildMeta(page)));
    }

    @GetMapping("/cycle/{cycleId}")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "List all goals in a review cycle")
    public ResponseEntity<ApiResponse<List<GoalDto.Response>>> listByCycle(
            @PathVariable UUID cycleId) {
        return ResponseEntity.ok(ApiResponse.ok(goalService.listByCycle(tenantId(), cycleId)));
    }

    // ── Progress ──────────────────────────────────────────────────────────────

    @PostMapping("/{id}/progress")
    @PreAuthorize("hasAuthority('PERFORMANCE:WRITE')")
    @Operation(summary = "Record a progress update; auto-recomputes parent Objective if Key Result")
    public ResponseEntity<ApiResponse<GoalDto.UpdateResponse>> updateProgress(
            @PathVariable UUID id,
            @Valid @RequestBody GoalDto.ProgressUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                goalService.updateProgress(tenantId(), id, req, currentUserId())));
    }

    @GetMapping("/{id}/updates")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "Get progress update history for a goal")
    public ResponseEntity<ApiResponse<List<GoalDto.UpdateResponse>>> getUpdates(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(goalService.getUpdates(tenantId(), id)));
    }

    private String tenantId() { return TenantContext.get(); }

    private UserPrincipal principal() {
        return (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    private String currentUserId() { return principal().getId(); }
}
