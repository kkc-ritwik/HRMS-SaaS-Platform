package com.hrms.document.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.document.dto.DocumentTypeDto;
import com.hrms.document.service.DocumentTypeService;
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
@RequestMapping("/api/v1/documents/types")
@RequiredArgsConstructor
@Tag(name = "Document Types", description = "Document type management")
public class DocumentTypeController {

    private final DocumentTypeService documentTypeService;

    @PostMapping
    @PreAuthorize("hasAuthority('DOCUMENT:WRITE')")
    @Operation(summary = "Create a document type")
    public ResponseEntity<ApiResponse<DocumentTypeDto.Response>> create(
            @Valid @RequestBody DocumentTypeDto.CreateRequest req) {
        DocumentTypeDto.Response response = documentTypeService.create(tenantId(), req, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DOCUMENT:READ')")
    @Operation(summary = "List document types with pagination")
    public ResponseEntity<ApiResponse<List<DocumentTypeDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DocumentTypeDto.Response> page = documentTypeService.list(tenantId(), pageable);
        PaginationMeta meta = documentTypeService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('DOCUMENT:READ')")
    @Operation(summary = "List all document types without pagination")
    public ResponseEntity<ApiResponse<List<DocumentTypeDto.Response>>> listAll() {
        return ResponseEntity.ok(ApiResponse.ok(documentTypeService.listAll(tenantId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT:READ')")
    @Operation(summary = "Get a document type by ID")
    public ResponseEntity<ApiResponse<DocumentTypeDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(documentTypeService.getById(tenantId(), id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT:WRITE')")
    @Operation(summary = "Update a document type")
    public ResponseEntity<ApiResponse<DocumentTypeDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DocumentTypeDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                documentTypeService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT:WRITE')")
    @Operation(summary = "Soft-delete a document type")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        documentTypeService.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    private String tenantId() {
        return TenantContext.get();
    }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
