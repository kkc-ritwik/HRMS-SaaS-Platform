package com.hrms.corehr.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.corehr.dto.EmployeeDto;
import com.hrms.corehr.entity.Employee;
import com.hrms.corehr.service.EmployeeService;
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
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee management and directory")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEES:WRITE')")
    @Operation(summary = "Onboard a new employee")
    public ResponseEntity<ApiResponse<EmployeeDto.Response>> create(
            @Valid @RequestBody EmployeeDto.CreateRequest req) {

        EmployeeDto.Response response = employeeService.create(tenantId(), req, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EMPLOYEES:READ')")
    @Operation(summary = "List employees with optional search and filters")
    public ResponseEntity<ApiResponse<List<EmployeeDto.ListItem>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) Employee.EmploymentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<EmployeeDto.ListItem> page = employeeService.list(
                tenantId(), search, departmentId, locationId, status, pageable);
        PaginationMeta meta = employeeService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/directory")
    @Operation(summary = "Employee directory (public profile cards)")
    public ResponseEntity<ApiResponse<List<EmployeeDto.DirectoryItem>>> directory(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<EmployeeDto.DirectoryItem> page = employeeService.directory(tenantId(), search, pageable);
        PaginationMeta meta = employeeService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEES:READ')")
    @Operation(summary = "Get employee full profile by ID")
    public ResponseEntity<ApiResponse<EmployeeDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(employeeService.getById(tenantId(), id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEES:WRITE')")
    @Operation(summary = "Update employee profile")
    public ResponseEntity<ApiResponse<EmployeeDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody EmployeeDto.UpdateRequest req) {

        return ResponseEntity.ok(ApiResponse.ok(
                employeeService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEES:DELETE')")
    @Operation(summary = "Soft-delete an employee record")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        employeeService.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/team")
    @PreAuthorize("hasAuthority('EMPLOYEES:READ')")
    @Operation(summary = "Get direct reports for a manager")
    public ResponseEntity<ApiResponse<List<EmployeeDto.ListItem>>> team(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                employeeService.getTeamMembers(tenantId(), id)));
    }

    @GetMapping("/{id}/lifecycle")
    @PreAuthorize("hasAuthority('EMPLOYEES:READ')")
    @Operation(summary = "Get lifecycle events for an employee")
    public ResponseEntity<ApiResponse<List<EmployeeDto.LifecycleEventResponse>>> lifecycle(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                employeeService.getLifecycleEvents(tenantId(), id)));
    }

    @PostMapping("/{id}/lifecycle-event")
    @PreAuthorize("hasAuthority('EMPLOYEES:WRITE')")
    @Operation(summary = "Record a manual lifecycle event for an employee")
    public ResponseEntity<ApiResponse<String>> addLifecycleEvent(
            @PathVariable UUID id,
            @Valid @RequestBody EmployeeDto.LifecycleEvent request) {
        employeeService.addLifecycleEvent(tenantId(), id, request, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Lifecycle event recorded"));
    }

    @GetMapping("/{id}/timeline")
    @PreAuthorize("hasAuthority('EMPLOYEES:READ')")
    @Operation(summary = "Get full history timeline for an employee (ordered by date DESC)")
    public ResponseEntity<ApiResponse<List<EmployeeDto.LifecycleEventResponse>>> timeline(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                employeeService.getTimeline(tenantId(), id)));
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
