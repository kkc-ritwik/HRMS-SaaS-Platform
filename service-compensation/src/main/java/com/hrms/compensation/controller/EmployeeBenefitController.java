package com.hrms.compensation.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.compensation.dto.EmployeeBenefitDto;
import com.hrms.compensation.service.EmployeeBenefitService;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/compensation/employee-benefits")
@RequiredArgsConstructor
public class EmployeeBenefitController {

    private final EmployeeBenefitService employeeBenefitService;

    @PostMapping
    @PreAuthorize("hasAuthority('COMPENSATION:WRITE')")
    public ResponseEntity<ApiResponse<EmployeeBenefitDto.Response>> create(
            @Valid @RequestBody EmployeeBenefitDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        EmployeeBenefitDto.Response response = employeeBenefitService.create(tenantId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPENSATION:READ')")
    public ResponseEntity<ApiResponse<EmployeeBenefitDto.Response>> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(employeeBenefitService.getById(tenantId, id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COMPENSATION:READ')")
    public ResponseEntity<ApiResponse<Page<EmployeeBenefitDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(employeeBenefitService.list(tenantId, pageable)));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('COMPENSATION:READ')")
    public ResponseEntity<ApiResponse<List<EmployeeBenefitDto.Response>>> listByEmployee(
            @PathVariable UUID employeeId) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(employeeBenefitService.listByEmployee(tenantId, employeeId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPENSATION:WRITE')")
    public ResponseEntity<ApiResponse<EmployeeBenefitDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody EmployeeBenefitDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(employeeBenefitService.update(tenantId, id, request, currentUser)));
    }

    @PostMapping("/{id}/terminate")
    @PreAuthorize("hasAuthority('COMPENSATION:WRITE')")
    public ResponseEntity<ApiResponse<EmployeeBenefitDto.Response>> terminate(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate terminationDate) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(employeeBenefitService.terminate(tenantId, id, terminationDate, currentUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPENSATION:WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        employeeBenefitService.delete(tenantId, id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
