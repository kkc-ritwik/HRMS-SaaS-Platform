package com.hrms.leave.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.leave.dto.LeavePolicyDto;
import com.hrms.leave.service.LeavePolicyService;
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
@RequestMapping("/api/v1/leaves/policies")
@RequiredArgsConstructor
@Tag(name = "Leave Policies", description = "Accrual, carry-forward and sandwich rule policies")
public class LeavePolicyController {

    private final LeavePolicyService leavePolicyService;

    @PostMapping
    @PreAuthorize("hasAuthority('LEAVE:WRITE')")
    @Operation(summary = "Create a leave policy")
    public ResponseEntity<ApiResponse<LeavePolicyDto.Response>> create(
            @Valid @RequestBody LeavePolicyDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(leavePolicyService.create(tenantId(), req, currentUserId())));
    }

    @GetMapping
    @Operation(summary = "List leave policies")
    public ResponseEntity<ApiResponse<List<LeavePolicyDto.Response>>> list(
            @PageableDefault(size = 50) Pageable pageable) {
        Page<LeavePolicyDto.Response> page = leavePolicyService.list(tenantId(), pageable);
        PaginationMeta meta = leavePolicyService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a leave policy by ID")
    public ResponseEntity<ApiResponse<LeavePolicyDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(leavePolicyService.getById(tenantId(), id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('LEAVE:WRITE')")
    @Operation(summary = "Update a leave policy")
    public ResponseEntity<ApiResponse<LeavePolicyDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody LeavePolicyDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                leavePolicyService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('LEAVE:WRITE')")
    @Operation(summary = "Delete a leave policy")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        leavePolicyService.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    private String tenantId() { return TenantContext.get(); }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
