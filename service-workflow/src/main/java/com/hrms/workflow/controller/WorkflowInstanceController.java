package com.hrms.workflow.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import com.hrms.workflow.dto.WorkflowInstanceDto;
import com.hrms.workflow.entity.WorkflowInstance.InstanceStatus;
import com.hrms.workflow.service.WorkflowInstanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows/instances")
@RequiredArgsConstructor
public class WorkflowInstanceController {

    private final WorkflowInstanceService service;

    @PostMapping
    @PreAuthorize("hasAuthority('WORKFLOW:WRITE')")
    public ResponseEntity<ApiResponse<WorkflowInstanceDto.Response>> create(
            @Valid @RequestBody WorkflowInstanceDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.create(tenantId, request, currentUser)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('WORKFLOW:READ')")
    public ResponseEntity<ApiResponse<WorkflowInstanceDto.Response>> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(service.getById(tenantId, id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('WORKFLOW:READ')")
    public ResponseEntity<ApiResponse<Page<WorkflowInstanceDto.Response>>> listAll(Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(service.listAll(tenantId, pageable)));
    }

    @GetMapping("/entity/{entityId}")
    @PreAuthorize("hasAuthority('WORKFLOW:READ')")
    public ResponseEntity<ApiResponse<List<WorkflowInstanceDto.Response>>> listByEntity(
            @PathVariable UUID entityId) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(service.listByEntity(tenantId, entityId)));
    }

    @GetMapping("/initiator/{initiatedBy}")
    @PreAuthorize("hasAuthority('WORKFLOW:READ')")
    public ResponseEntity<ApiResponse<Page<WorkflowInstanceDto.Response>>> listByInitiator(
            @PathVariable UUID initiatedBy, Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(service.listByInitiator(tenantId, initiatedBy, pageable)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('WORKFLOW:READ')")
    public ResponseEntity<ApiResponse<Page<WorkflowInstanceDto.Response>>> listByStatus(
            @PathVariable InstanceStatus status, Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(service.listByStatus(tenantId, status, pageable)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WORKFLOW:WRITE')")
    public ResponseEntity<ApiResponse<WorkflowInstanceDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody WorkflowInstanceDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(service.update(tenantId, id, request, currentUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WORKFLOW:WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        service.delete(tenantId, id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('WORKFLOW:WRITE')")
    public ResponseEntity<ApiResponse<WorkflowInstanceDto.Response>> approve(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(service.approve(tenantId, id, currentUser)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('WORKFLOW:WRITE')")
    public ResponseEntity<ApiResponse<WorkflowInstanceDto.Response>> reject(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        String notes = body.get("notes");
        return ResponseEntity.ok(ApiResponse.ok(service.reject(tenantId, id, notes, currentUser)));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('WORKFLOW:WRITE')")
    public ResponseEntity<ApiResponse<WorkflowInstanceDto.Response>> cancel(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(service.cancel(tenantId, id, currentUser)));
    }
}
