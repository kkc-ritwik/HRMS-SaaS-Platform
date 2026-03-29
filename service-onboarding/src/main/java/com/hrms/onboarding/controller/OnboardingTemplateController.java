package com.hrms.onboarding.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.onboarding.dto.OnboardingTemplateDto;
import com.hrms.onboarding.service.OnboardingTemplateService;
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
@RequestMapping("/api/v1/onboarding/templates")
@RequiredArgsConstructor
@Tag(name = "Onboarding Templates", description = "Manage onboarding templates")
public class OnboardingTemplateController {

    private final OnboardingTemplateService onboardingTemplateService;

    @PostMapping
    @PreAuthorize("hasAuthority('ONBOARDING:WRITE')")
    @Operation(summary = "Create an onboarding template")
    public ResponseEntity<ApiResponse<OnboardingTemplateDto.Response>> create(
            @Valid @RequestBody OnboardingTemplateDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(onboardingTemplateService.create(tenantId(), req, currentUserId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ONBOARDING:READ')")
    @Operation(summary = "Get an onboarding template by ID")
    public ResponseEntity<ApiResponse<OnboardingTemplateDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(onboardingTemplateService.get(tenantId(), id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ONBOARDING:READ')")
    @Operation(summary = "List onboarding templates with pagination")
    public ResponseEntity<ApiResponse<List<OnboardingTemplateDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OnboardingTemplateDto.Response> page = onboardingTemplateService.list(tenantId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), onboardingTemplateService.buildMeta(page)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ONBOARDING:WRITE')")
    @Operation(summary = "Update an onboarding template")
    public ResponseEntity<ApiResponse<OnboardingTemplateDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody OnboardingTemplateDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(onboardingTemplateService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ONBOARDING:WRITE')")
    @Operation(summary = "Delete an onboarding template")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        onboardingTemplateService.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    private String tenantId() {
        return TenantContext.get();
    }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}
