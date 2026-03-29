package com.hrms.document.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.document.dto.DocumentTemplateDto;
import com.hrms.document.service.DocumentTemplateService;
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
@RequestMapping("/api/v1/documents/templates")
@RequiredArgsConstructor
@Tag(name = "Document Templates", description = "Document template management")
public class DocumentTemplateController {

    private final DocumentTemplateService documentTemplateService;

    @PostMapping
    @PreAuthorize("hasAuthority('DOCUMENT:WRITE')")
    @Operation(summary = "Create a document template")
    public ResponseEntity<ApiResponse<DocumentTemplateDto.Response>> create(
            @Valid @RequestBody DocumentTemplateDto.CreateRequest req) {
        DocumentTemplateDto.Response response = documentTemplateService.create(tenantId(), req, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DOCUMENT:READ')")
    @Operation(summary = "List document templates with pagination")
    public ResponseEntity<ApiResponse<List<DocumentTemplateDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DocumentTemplateDto.Response> page = documentTemplateService.list(tenantId(), pageable);
        PaginationMeta meta = documentTemplateService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('DOCUMENT:READ')")
    @Operation(summary = "List all document templates without pagination")
    public ResponseEntity<ApiResponse<List<DocumentTemplateDto.Response>>> listAll() {
        return ResponseEntity.ok(ApiResponse.ok(documentTemplateService.listAll(tenantId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT:READ')")
    @Operation(summary = "Get a document template by ID")
    public ResponseEntity<ApiResponse<DocumentTemplateDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(documentTemplateService.getById(tenantId(), id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT:WRITE')")
    @Operation(summary = "Update a document template")
    public ResponseEntity<ApiResponse<DocumentTemplateDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DocumentTemplateDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                documentTemplateService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT:WRITE')")
    @Operation(summary = "Soft-delete a document template")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        documentTemplateService.delete(tenantId(), id, currentUserId());
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
