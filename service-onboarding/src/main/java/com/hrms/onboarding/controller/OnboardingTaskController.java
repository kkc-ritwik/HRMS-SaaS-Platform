package com.hrms.onboarding.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.onboarding.dto.OnboardingTaskDto;
import com.hrms.onboarding.service.OnboardingTaskService;
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
@RequestMapping("/api/v1/onboarding/tasks")
@RequiredArgsConstructor
@Tag(name = "Onboarding Tasks", description = "Manage onboarding tasks")
public class OnboardingTaskController {

    private final OnboardingTaskService onboardingTaskService;

    @PostMapping
    @PreAuthorize("hasAuthority('ONBOARDING:WRITE')")
    @Operation(summary = "Create an onboarding task")
    public ResponseEntity<ApiResponse<OnboardingTaskDto.Response>> create(
            @Valid @RequestBody OnboardingTaskDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(onboardingTaskService.create(tenantId(), req, currentUserId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ONBOARDING:READ')")
    @Operation(summary = "Get an onboarding task by ID")
    public ResponseEntity<ApiResponse<OnboardingTaskDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(onboardingTaskService.get(tenantId(), id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ONBOARDING:READ')")
    @Operation(summary = "List onboarding tasks with pagination")
    public ResponseEntity<ApiResponse<List<OnboardingTaskDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OnboardingTaskDto.Response> page = onboardingTaskService.list(tenantId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), onboardingTaskService.buildMeta(page)));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('ONBOARDING:READ')")
    @Operation(summary = "List onboarding tasks by employee")
    public ResponseEntity<ApiResponse<List<OnboardingTaskDto.Response>>> listByEmployee(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(onboardingTaskService.listByEmployee(tenantId(), employeeId)));
    }

    @GetMapping("/template/{templateId}")
    @PreAuthorize("hasAuthority('ONBOARDING:READ')")
    @Operation(summary = "List onboarding tasks by template")
    public ResponseEntity<ApiResponse<List<OnboardingTaskDto.Response>>> listByTemplate(
            @PathVariable UUID templateId) {
        return ResponseEntity.ok(ApiResponse.ok(onboardingTaskService.listByTemplate(tenantId(), templateId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ONBOARDING:WRITE')")
    @Operation(summary = "Update an onboarding task")
    public ResponseEntity<ApiResponse<OnboardingTaskDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody OnboardingTaskDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(onboardingTaskService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ONBOARDING:WRITE')")
    @Operation(summary = "Delete an onboarding task")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        onboardingTaskService.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    private String tenantId() {
        return TenantContext.get();
    }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}
