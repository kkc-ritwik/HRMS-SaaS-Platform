package com.hrms.performance.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.performance.dto.OneOnOneDto;
import com.hrms.performance.service.OneOnOneService;
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
@RequestMapping("/api/v1/one-on-ones")
@RequiredArgsConstructor
@Tag(name = "1-on-1 Meetings", description = "Schedule and track manager-employee 1-on-1 meetings with action items")
public class OneOnOneController {

    private final OneOnOneService oneOnOneService;

    @PostMapping
    @PreAuthorize("hasAuthority('PERFORMANCE:WRITE')")
    @Operation(summary = "Schedule a 1-on-1 meeting (manager schedules with employee)")
    public ResponseEntity<ApiResponse<OneOnOneDto.Response>> schedule(
            @Valid @RequestBody OneOnOneDto.ScheduleRequest req) {
        UUID managerId = UUID.fromString(principal().getEmployeeId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(oneOnOneService.schedule(tenantId(), managerId, req, currentUserId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFORMANCE:WRITE')")
    @Operation(summary = "Update a meeting (reschedule, add notes/action items)")
    public ResponseEntity<ApiResponse<OneOnOneDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody OneOnOneDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                oneOnOneService.update(tenantId(), id, req, currentUserId())));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('PERFORMANCE:WRITE')")
    @Operation(summary = "Mark a meeting as COMPLETED")
    public ResponseEntity<ApiResponse<OneOnOneDto.Response>> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                oneOnOneService.complete(tenantId(), id, currentUserId())));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "List the current user's 1-on-1 meetings (as employee)")
    public ResponseEntity<ApiResponse<List<OneOnOneDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        String empId = principal().getEmployeeId();
        if (empId == null) return ResponseEntity.ok(ApiResponse.ok(List.of()));
        Page<OneOnOneDto.Response> page =
                oneOnOneService.listForEmployee(tenantId(), UUID.fromString(empId), pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), oneOnOneService.buildMeta(page)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "Get a 1-on-1 meeting by ID")
    public ResponseEntity<ApiResponse<OneOnOneDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(oneOnOneService.get(tenantId(), id)));
    }

    // ── Self-service ──────────────────────────────────────────────────────────

    @GetMapping("/me/as-employee")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "My meetings as an employee")
    public ResponseEntity<ApiResponse<List<OneOnOneDto.Response>>> myMeetingsAsEmployee(
            @PageableDefault(size = 20) Pageable pageable) {
        UUID employeeId = UUID.fromString(principal().getEmployeeId());
        Page<OneOnOneDto.Response> page =
                oneOnOneService.listForEmployee(tenantId(), employeeId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), oneOnOneService.buildMeta(page)));
    }

    @GetMapping("/me/as-manager")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "All meetings I have scheduled as a manager")
    public ResponseEntity<ApiResponse<List<OneOnOneDto.Response>>> myMeetingsAsManager(
            @PageableDefault(size = 20) Pageable pageable) {
        UUID managerId = UUID.fromString(principal().getEmployeeId());
        Page<OneOnOneDto.Response> page =
                oneOnOneService.listForManager(tenantId(), managerId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), oneOnOneService.buildMeta(page)));
    }

    // ── HR / admin views ──────────────────────────────────────────────────────

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "List meetings for a specific employee")
    public ResponseEntity<ApiResponse<List<OneOnOneDto.Response>>> listForEmployee(
            @PathVariable UUID employeeId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OneOnOneDto.Response> page =
                oneOnOneService.listForEmployee(tenantId(), employeeId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), oneOnOneService.buildMeta(page)));
    }

    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "List all meetings for a specific manager")
    public ResponseEntity<ApiResponse<List<OneOnOneDto.Response>>> listForManager(
            @PathVariable UUID managerId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OneOnOneDto.Response> page =
                oneOnOneService.listForManager(tenantId(), managerId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), oneOnOneService.buildMeta(page)));
    }

    @GetMapping("/between")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "List meetings between a specific manager and employee pair")
    public ResponseEntity<ApiResponse<List<OneOnOneDto.Response>>> listBetween(
            @RequestParam UUID managerId,
            @RequestParam UUID employeeId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OneOnOneDto.Response> page =
                oneOnOneService.listBetween(tenantId(), managerId, employeeId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), oneOnOneService.buildMeta(page)));
    }

    private String tenantId() { return TenantContext.get(); }

    private UserPrincipal principal() {
        return (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    private String currentUserId() { return principal().getId(); }
}
