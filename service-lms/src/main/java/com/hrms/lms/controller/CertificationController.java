package com.hrms.lms.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.lms.dto.CertificationDto;
import com.hrms.lms.service.CertificationService;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses/certifications")
@RequiredArgsConstructor
public class CertificationController {

    private final CertificationService certificationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('LMS:WRITE')")
    public ApiResponse<CertificationDto.Response> create(@Valid @RequestBody CertificationDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ApiResponse.ok(certificationService.create(tenantId, request, currentUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LMS:READ')")
    public ApiResponse<CertificationDto.Response> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(certificationService.getById(tenantId, id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LMS:READ')")
    public ApiResponse<Page<CertificationDto.Response>> list(@PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(certificationService.list(tenantId, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('LMS:WRITE')")
    public ApiResponse<CertificationDto.Response> update(@PathVariable UUID id,
                                                         @Valid @RequestBody CertificationDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ApiResponse.ok(certificationService.update(tenantId, id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('LMS:WRITE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        certificationService.delete(tenantId, id, currentUser);
        return ApiResponse.ok(null);
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('LMS:READ')")
    public ApiResponse<List<CertificationDto.Response>> listByEmployee(@PathVariable UUID employeeId) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(certificationService.listByEmployee(tenantId, employeeId));
    }

    @PostMapping("/{id}/revoke")
    @PreAuthorize("hasAuthority('LMS:WRITE')")
    public ApiResponse<CertificationDto.Response> revoke(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ApiResponse.ok(certificationService.revoke(tenantId, id, currentUser));
    }
}
