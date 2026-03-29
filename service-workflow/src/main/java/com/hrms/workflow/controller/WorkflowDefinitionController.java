package com.hrms.workflow.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import com.hrms.workflow.dto.WorkflowDefinitionDto;
import com.hrms.workflow.service.WorkflowDefinitionService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows/definitions")
@RequiredArgsConstructor
public class WorkflowDefinitionController {

    private final WorkflowDefinitionService service;

    @PostMapping
    @PreAuthorize("hasAuthority('WORKFLOW:WRITE')")
    public ResponseEntity<ApiResponse<WorkflowDefinitionDto.Response>> create(
            @Valid @RequestBody WorkflowDefinitionDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.create(tenantId, request, currentUser)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('WORKFLOW:READ')")
    public ResponseEntity<ApiResponse<WorkflowDefinitionDto.Response>> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(service.getById(tenantId, id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('WORKFLOW:READ')")
    public ResponseEntity<ApiResponse<Page<WorkflowDefinitionDto.Response>>> listAll(Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(service.listAll(tenantId, pageable)));
    }

    @GetMapping("/entity/{entityType}")
    @PreAuthorize("hasAuthority('WORKFLOW:READ')")
    public ResponseEntity<ApiResponse<List<WorkflowDefinitionDto.Response>>> listByEntityType(
            @PathVariable String entityType) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(service.listByEntityType(tenantId, entityType)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WORKFLOW:WRITE')")
    public ResponseEntity<ApiResponse<WorkflowDefinitionDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody WorkflowDefinitionDto.UpdateRequest request) {
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

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('WORKFLOW:WRITE')")
    public ResponseEntity<ApiResponse<WorkflowDefinitionDto.Response>> activate(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(service.activate(tenantId, id, currentUser)));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('WORKFLOW:WRITE')")
    public ResponseEntity<ApiResponse<WorkflowDefinitionDto.Response>> deactivate(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(service.deactivate(tenantId, id, currentUser)));
    }
}
