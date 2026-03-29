package com.hrms.document.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.document.dto.GeneratedLetterDto;
import com.hrms.document.service.GeneratedLetterService;
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
@RequestMapping("/api/v1/documents/letters")
@RequiredArgsConstructor
@Tag(name = "Generated Letters", description = "Generated letter management")
public class GeneratedLetterController {

    private final GeneratedLetterService generatedLetterService;

    @PostMapping
    @PreAuthorize("hasAuthority('DOCUMENT:WRITE')")
    @Operation(summary = "Generate a letter")
    public ResponseEntity<ApiResponse<GeneratedLetterDto.Response>> create(
            @Valid @RequestBody GeneratedLetterDto.CreateRequest req) {
        GeneratedLetterDto.Response response = generatedLetterService.create(tenantId(), req, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DOCUMENT:READ')")
    @Operation(summary = "List generated letters with pagination")
    public ResponseEntity<ApiResponse<List<GeneratedLetterDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GeneratedLetterDto.Response> page = generatedLetterService.list(tenantId(), pageable);
        PaginationMeta meta = generatedLetterService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT:READ')")
    @Operation(summary = "Get a generated letter by ID")
    public ResponseEntity<ApiResponse<GeneratedLetterDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(generatedLetterService.getById(tenantId(), id)));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('DOCUMENT:READ')")
    @Operation(summary = "List all generated letters for a specific employee")
    public ResponseEntity<ApiResponse<List<GeneratedLetterDto.Response>>> listByEmployee(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(
                generatedLetterService.listByEmployee(tenantId(), employeeId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT:WRITE')")
    @Operation(summary = "Update a generated letter")
    public ResponseEntity<ApiResponse<GeneratedLetterDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody GeneratedLetterDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                generatedLetterService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT:WRITE')")
    @Operation(summary = "Soft-delete a generated letter")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        generatedLetterService.delete(tenantId(), id, currentUserId());
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
