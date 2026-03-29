package com.hrms.notification.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.notification.dto.EmailTemplateDto;
import com.hrms.notification.service.EmailTemplateService;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications/email-templates")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    @PostMapping
    @PreAuthorize("hasAuthority('NOTIFICATION:WRITE')")
    public ResponseEntity<ApiResponse<EmailTemplateDto.Response>> create(
            @Valid @RequestBody EmailTemplateDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        EmailTemplateDto.Response response = emailTemplateService.create(tenantId, currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION:READ')")
    public ResponseEntity<ApiResponse<EmailTemplateDto.Response>> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        EmailTemplateDto.Response response = emailTemplateService.getById(tenantId, id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('NOTIFICATION:READ')")
    public ResponseEntity<ApiResponse<List<EmailTemplateDto.Response>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String tenantId = TenantContext.get();
        Pageable pageable = PageRequest.of(page, size);
        Page<EmailTemplateDto.Response> result = emailTemplateService.list(tenantId, pageable);
        PaginationMeta meta = PaginationMeta.builder()
                .page(result.getNumber())
                .size(result.getSize())
                .total(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .hasNext(result.hasNext())
                .hasPrevious(result.hasPrevious())
                .build();
        return ResponseEntity.ok(ApiResponse.ok(result.getContent(), meta));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('NOTIFICATION:READ')")
    public ResponseEntity<ApiResponse<EmailTemplateDto.Response>> getByCode(
            @PathVariable String code) {
        String tenantId = TenantContext.get();
        EmailTemplateDto.Response response = emailTemplateService.findByCode(tenantId, code);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION:WRITE')")
    public ResponseEntity<ApiResponse<EmailTemplateDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody EmailTemplateDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        EmailTemplateDto.Response response =
                emailTemplateService.update(tenantId, id, currentUser, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION:WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        emailTemplateService.delete(tenantId, id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
