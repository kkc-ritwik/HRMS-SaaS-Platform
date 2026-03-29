package com.hrms.corehr.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.corehr.dto.OrgChartNode;
import com.hrms.corehr.service.EmployeeService;
import com.hrms.security.model.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/org-chart")
@RequiredArgsConstructor
@Tag(name = "Org Chart", description = "Hierarchical org chart built from reporting-manager relationships")
public class OrgChartController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAuthority('EMPLOYEES:READ')")
    @Operation(summary = "Get full org chart (all root nodes and their subtrees)")
    public ResponseEntity<ApiResponse<List<OrgChartNode>>> getOrgChart() {
        return ResponseEntity.ok(ApiResponse.ok(
                employeeService.buildOrgChart(TenantContext.get())));
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('EMPLOYEES:READ')")
    @Operation(summary = "Get org chart subtree rooted at a specific employee")
    public ResponseEntity<ApiResponse<OrgChartNode>> getSubTree(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(
                employeeService.buildOrgChartFrom(TenantContext.get(), employeeId)));
    }
}
