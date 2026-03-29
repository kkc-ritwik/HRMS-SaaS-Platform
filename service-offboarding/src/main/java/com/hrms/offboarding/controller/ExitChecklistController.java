package com.hrms.offboarding.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.offboarding.dto.ExitChecklistDto;
import com.hrms.offboarding.service.ExitChecklistService;
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
@RequestMapping("/api/v1/offboarding/checklists")
@RequiredArgsConstructor
@Tag(name = "Exit Checklists", description = "Manage exit checklist tasks")
public class ExitChecklistController {

    private final ExitChecklistService exitChecklistService;

    @PostMapping
    @PreAuthorize("hasAuthority('OFFBOARDING:WRITE')")
    @Operation(summary = "Create a checklist task")
    public ResponseEntity<ApiResponse<ExitChecklistDto.Response>> create(
            @Valid @RequestBody ExitChecklistDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(exitChecklistService.create(tenantId(), req, currentUserId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('OFFBOARDING:READ')")
    @Operation(summary = "Get a checklist task by ID")
    public ResponseEntity<ApiResponse<ExitChecklistDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(exitChecklistService.get(tenantId(), id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('OFFBOARDING:READ')")
    @Operation(summary = "List all checklist tasks with pagination")
    public ResponseEntity<ApiResponse<List<ExitChecklistDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ExitChecklistDto.Response> page = exitChecklistService.list(tenantId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), exitChecklistService.buildMeta(page)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('OFFBOARDING:WRITE')")
    @Operation(summary = "Update a checklist task")
    public ResponseEntity<ApiResponse<ExitChecklistDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ExitChecklistDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(exitChecklistService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('OFFBOARDING:WRITE')")
    @Operation(summary = "Soft-delete a checklist task")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        exitChecklistService.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/separation/{separationId}")
    @PreAuthorize("hasAuthority('OFFBOARDING:READ')")
    @Operation(summary = "List checklist tasks by separation")
    public ResponseEntity<ApiResponse<List<ExitChecklistDto.Response>>> listBySeparation(
            @PathVariable UUID separationId) {
        return ResponseEntity.ok(ApiResponse.ok(exitChecklistService.listBySeparation(tenantId(), separationId)));
    }

    @GetMapping("/assignee/{assignedTo}")
    @PreAuthorize("hasAuthority('OFFBOARDING:READ')")
    @Operation(summary = "List checklist tasks by assignee")
    public ResponseEntity<ApiResponse<List<ExitChecklistDto.Response>>> listByAssignee(
            @PathVariable UUID assignedTo) {
        return ResponseEntity.ok(ApiResponse.ok(exitChecklistService.listByAssignee(tenantId(), assignedTo)));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('OFFBOARDING:WRITE')")
    @Operation(summary = "Mark a checklist task as complete")
    public ResponseEntity<ApiResponse<ExitChecklistDto.Response>> markComplete(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(exitChecklistService.markComplete(tenantId(), id, currentUserId())));
    }

    private String tenantId() {
        return TenantContext.get();
    }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}
