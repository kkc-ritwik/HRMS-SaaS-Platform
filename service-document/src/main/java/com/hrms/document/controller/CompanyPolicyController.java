package com.hrms.document.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.document.dto.CompanyPolicyDto;
import com.hrms.document.service.CompanyPolicyService;
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
@RequestMapping("/api/v1/documents/policies")
@RequiredArgsConstructor
@Tag(name = "Company Policies", description = "Company policy management")
public class CompanyPolicyController {

    private final CompanyPolicyService companyPolicyService;

    @PostMapping
    @PreAuthorize("hasAuthority('DOCUMENT:WRITE')")
    @Operation(summary = "Create a company policy")
    public ResponseEntity<ApiResponse<CompanyPolicyDto.Response>> create(
            @Valid @RequestBody CompanyPolicyDto.CreateRequest req) {
        CompanyPolicyDto.Response response = companyPolicyService.create(tenantId(), req, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DOCUMENT:READ')")
    @Operation(summary = "List company policies with pagination")
    public ResponseEntity<ApiResponse<List<CompanyPolicyDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CompanyPolicyDto.Response> page = companyPolicyService.list(tenantId(), pageable);
        PaginationMeta meta = companyPolicyService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('DOCUMENT:READ')")
    @Operation(summary = "List all company policies without pagination")
    public ResponseEntity<ApiResponse<List<CompanyPolicyDto.Response>>> listAll() {
        return ResponseEntity.ok(ApiResponse.ok(companyPolicyService.listAll(tenantId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT:READ')")
    @Operation(summary = "Get a company policy by ID")
    public ResponseEntity<ApiResponse<CompanyPolicyDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(companyPolicyService.getById(tenantId(), id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT:WRITE')")
    @Operation(summary = "Update a company policy")
    public ResponseEntity<ApiResponse<CompanyPolicyDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CompanyPolicyDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                companyPolicyService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT:WRITE')")
    @Operation(summary = "Soft-delete a company policy")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        companyPolicyService.delete(tenantId(), id, currentUserId());
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
