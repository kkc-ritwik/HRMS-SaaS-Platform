package com.hrms.onboarding.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.onboarding.dto.OnboardingDocumentDto;
import com.hrms.onboarding.service.OnboardingDocumentService;
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
@RequestMapping("/api/v1/onboarding/documents")
@RequiredArgsConstructor
@Tag(name = "Onboarding Documents", description = "Manage onboarding documents")
public class OnboardingDocumentController {

    private final OnboardingDocumentService onboardingDocumentService;

    @PostMapping
    @PreAuthorize("hasAuthority('ONBOARDING:WRITE')")
    @Operation(summary = "Create an onboarding document")
    public ResponseEntity<ApiResponse<OnboardingDocumentDto.Response>> create(
            @Valid @RequestBody OnboardingDocumentDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(onboardingDocumentService.create(tenantId(), req, currentUserId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ONBOARDING:READ')")
    @Operation(summary = "Get an onboarding document by ID")
    public ResponseEntity<ApiResponse<OnboardingDocumentDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(onboardingDocumentService.get(tenantId(), id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ONBOARDING:READ')")
    @Operation(summary = "List onboarding documents with pagination")
    public ResponseEntity<ApiResponse<List<OnboardingDocumentDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OnboardingDocumentDto.Response> page = onboardingDocumentService.list(tenantId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), onboardingDocumentService.buildMeta(page)));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('ONBOARDING:READ')")
    @Operation(summary = "List onboarding documents by employee")
    public ResponseEntity<ApiResponse<List<OnboardingDocumentDto.Response>>> listByEmployee(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(onboardingDocumentService.listByEmployee(tenantId(), employeeId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ONBOARDING:WRITE')")
    @Operation(summary = "Update an onboarding document")
    public ResponseEntity<ApiResponse<OnboardingDocumentDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody OnboardingDocumentDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(onboardingDocumentService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ONBOARDING:WRITE')")
    @Operation(summary = "Delete an onboarding document")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        onboardingDocumentService.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    private String tenantId() {
        return TenantContext.get();
    }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}
