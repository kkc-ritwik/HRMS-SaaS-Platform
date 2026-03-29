package com.hrms.payroll.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.payroll.dto.EmployeeSalaryDto;
import com.hrms.payroll.service.EmployeeSalaryService;
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
@RequestMapping("/api/v1/salary/employee")
@RequiredArgsConstructor
@Tag(name = "Employee Salary", description = "Assign and manage employee salary revisions and component breakdowns")
public class EmployeeSalaryController {

    private final EmployeeSalaryService employeeSalaryService;

    @PostMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Assign or revise salary for an employee")
    public ResponseEntity<ApiResponse<EmployeeSalaryDto.Response>> assign(
            @PathVariable UUID employeeId,
            @Valid @RequestBody EmployeeSalaryDto.AssignRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                employeeSalaryService.assign(tenantId(), employeeId, req, currentUserId())));
    }

    @GetMapping("/{employeeId}/current")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get the current active salary for an employee")
    public ResponseEntity<ApiResponse<EmployeeSalaryDto.Response>> getCurrent(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(
                employeeSalaryService.getCurrentSalary(tenantId(), employeeId)));
    }

    @GetMapping("/{employeeId}/history")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get full salary revision history for an employee")
    public ResponseEntity<ApiResponse<List<EmployeeSalaryDto.Response>>> history(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(
                employeeSalaryService.getSalaryHistory(tenantId(), employeeId)));
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get salary records for an employee (paginated)")
    public ResponseEntity<ApiResponse<List<EmployeeSalaryDto.Response>>> list(
            @PathVariable UUID employeeId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<EmployeeSalaryDto.Response> page =
                employeeSalaryService.getSalaryPage(tenantId(), employeeId, pageable);
        PaginationMeta meta = employeeSalaryService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/{employeeId}/salary/{salaryId}")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get a specific salary revision by ID")
    public ResponseEntity<ApiResponse<EmployeeSalaryDto.Response>> getById(
            @PathVariable UUID employeeId,
            @PathVariable UUID salaryId) {
        return ResponseEntity.ok(ApiResponse.ok(
                employeeSalaryService.getSalaryById(tenantId(), salaryId)));
    }

    private String tenantId()     { return TenantContext.get(); }
    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
