package com.hrms.leave.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.leave.dto.LeaveTypeDto;
import com.hrms.leave.service.LeaveTypeService;
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
@RequestMapping("/api/v1/leaves/types")
@RequiredArgsConstructor
@Tag(name = "Leave Types", description = "Manage leave type definitions")
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    @PostMapping
    @PreAuthorize("hasAuthority('LEAVE:WRITE')")
    @Operation(summary = "Create a leave type")
    public ResponseEntity<ApiResponse<LeaveTypeDto.Response>> create(
            @Valid @RequestBody LeaveTypeDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(leaveTypeService.create(tenantId(), req, currentUserId())));
    }

    @GetMapping
    @Operation(summary = "List leave types")
    public ResponseEntity<ApiResponse<List<LeaveTypeDto.Response>>> list(
            @PageableDefault(size = 50) Pageable pageable) {
        Page<LeaveTypeDto.Response> page = leaveTypeService.list(tenantId(), pageable);
        PaginationMeta meta = leaveTypeService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/active")
    @Operation(summary = "List active leave types (for dropdowns)")
    public ResponseEntity<ApiResponse<List<LeaveTypeDto.Response>>> listActive() {
        return ResponseEntity.ok(ApiResponse.ok(leaveTypeService.listActive(tenantId())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a leave type by ID")
    public ResponseEntity<ApiResponse<LeaveTypeDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(leaveTypeService.getById(tenantId(), id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('LEAVE:WRITE')")
    @Operation(summary = "Update a leave type")
    public ResponseEntity<ApiResponse<LeaveTypeDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody LeaveTypeDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                leaveTypeService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('LEAVE:WRITE')")
    @Operation(summary = "Delete a leave type")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        leaveTypeService.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    private String tenantId() { return TenantContext.get(); }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
