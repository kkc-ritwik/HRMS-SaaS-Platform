package com.hrms.offboarding.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.offboarding.dto.KnowledgeTransferDto;
import com.hrms.offboarding.service.KnowledgeTransferService;
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
@RequestMapping("/api/v1/offboarding/knowledge-transfers")
@RequiredArgsConstructor
@Tag(name = "Knowledge Transfers", description = "Manage knowledge transfers during offboarding")
public class KnowledgeTransferController {

    private final KnowledgeTransferService knowledgeTransferService;

    @PostMapping
    @PreAuthorize("hasAuthority('OFFBOARDING:WRITE')")
    @Operation(summary = "Create a knowledge transfer record")
    public ResponseEntity<ApiResponse<KnowledgeTransferDto.Response>> create(
            @Valid @RequestBody KnowledgeTransferDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(knowledgeTransferService.create(tenantId(), req, currentUserId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('OFFBOARDING:READ')")
    @Operation(summary = "Get a knowledge transfer by ID")
    public ResponseEntity<ApiResponse<KnowledgeTransferDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(knowledgeTransferService.get(tenantId(), id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('OFFBOARDING:READ')")
    @Operation(summary = "List all knowledge transfers with pagination")
    public ResponseEntity<ApiResponse<List<KnowledgeTransferDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<KnowledgeTransferDto.Response> page = knowledgeTransferService.list(tenantId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), knowledgeTransferService.buildMeta(page)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('OFFBOARDING:WRITE')")
    @Operation(summary = "Update a knowledge transfer record")
    public ResponseEntity<ApiResponse<KnowledgeTransferDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody KnowledgeTransferDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(knowledgeTransferService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('OFFBOARDING:WRITE')")
    @Operation(summary = "Soft-delete a knowledge transfer record")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        knowledgeTransferService.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/separation/{separationId}")
    @PreAuthorize("hasAuthority('OFFBOARDING:READ')")
    @Operation(summary = "List knowledge transfers by separation")
    public ResponseEntity<ApiResponse<List<KnowledgeTransferDto.Response>>> listBySeparation(
            @PathVariable UUID separationId) {
        return ResponseEntity.ok(ApiResponse.ok(knowledgeTransferService.listBySeparation(tenantId(), separationId)));
    }

    @GetMapping("/from/{employeeId}")
    @PreAuthorize("hasAuthority('OFFBOARDING:READ')")
    @Operation(summary = "List knowledge transfers where the employee is the knowledge source")
    public ResponseEntity<ApiResponse<List<KnowledgeTransferDto.Response>>> listByFromEmployee(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(knowledgeTransferService.listByFromEmployee(tenantId(), employeeId)));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('OFFBOARDING:WRITE')")
    @Operation(summary = "Mark a knowledge transfer as completed")
    public ResponseEntity<ApiResponse<KnowledgeTransferDto.Response>> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(knowledgeTransferService.complete(tenantId(), id, currentUserId())));
    }

    private String tenantId() {
        return TenantContext.get();
    }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}
