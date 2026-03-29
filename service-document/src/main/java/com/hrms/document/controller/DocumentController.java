package com.hrms.document.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.document.dto.DocumentDto;
import com.hrms.document.service.DocumentService;
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
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Document management")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    @PreAuthorize("hasAuthority('DOCUMENT:WRITE')")
    @Operation(summary = "Upload / create a document record")
    public ResponseEntity<ApiResponse<DocumentDto.Response>> create(
            @Valid @RequestBody DocumentDto.CreateRequest req) {
        DocumentDto.Response response = documentService.create(tenantId(), req, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DOCUMENT:READ')")
    @Operation(summary = "List documents with pagination")
    public ResponseEntity<ApiResponse<List<DocumentDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DocumentDto.Response> page = documentService.list(tenantId(), pageable);
        PaginationMeta meta = documentService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT:READ')")
    @Operation(summary = "Get a document by ID")
    public ResponseEntity<ApiResponse<DocumentDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(documentService.getById(tenantId(), id)));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('DOCUMENT:READ')")
    @Operation(summary = "List all documents for a specific employee")
    public ResponseEntity<ApiResponse<List<DocumentDto.Response>>> listByEmployee(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(
                documentService.listByEmployee(tenantId(), employeeId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT:WRITE')")
    @Operation(summary = "Update a document")
    public ResponseEntity<ApiResponse<DocumentDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DocumentDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                documentService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT:WRITE')")
    @Operation(summary = "Soft-delete a document")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        documentService.delete(tenantId(), id, currentUserId());
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
