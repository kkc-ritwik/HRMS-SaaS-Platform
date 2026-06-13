package com.hrms.helpdesk.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.helpdesk.dto.KbArticleDto;
import com.hrms.helpdesk.service.KbArticleService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets/kb-articles")
@RequiredArgsConstructor
public class KbArticleController {

    private final KbArticleService kbArticleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('HELPDESK:WRITE')")
    public ApiResponse<KbArticleDto.Response> create(
            @Valid @RequestBody KbArticleDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = getCurrentUserId();
        return ApiResponse.ok(kbArticleService.create(tenantId, currentUser, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('HELPDESK:READ')")
    public ApiResponse<KbArticleDto.Response> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(kbArticleService.getById(tenantId, id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('HELPDESK:READ')")
    public ApiResponse<Page<KbArticleDto.Response>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(kbArticleService.list(tenantId, pageable));
    }

    @GetMapping("/published")
    @PreAuthorize("hasAuthority('HELPDESK:READ')")
    public ApiResponse<Page<KbArticleDto.Response>> listPublished(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(kbArticleService.listPublished(tenantId, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HELPDESK:WRITE')")
    public ApiResponse<KbArticleDto.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody KbArticleDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = getCurrentUserId();
        return ApiResponse.ok(kbArticleService.update(tenantId, id, currentUser, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HELPDESK:WRITE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = getCurrentUserId();
        kbArticleService.delete(tenantId, id, currentUser);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('HELPDESK:WRITE')")
    public ApiResponse<KbArticleDto.Response> publish(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = getCurrentUserId();
        return ApiResponse.ok(kbArticleService.publish(tenantId, id, currentUser));
    }

    @PostMapping("/{id}/view")
    @PreAuthorize("hasAuthority('HELPDESK:READ')")
    public ApiResponse<KbArticleDto.Response> incrementViews(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(kbArticleService.incrementViews(tenantId, id));
    }

    @PostMapping("/{id}/feedback")
    @PreAuthorize("hasAuthority('HELPDESK:READ')")
    public ApiResponse<KbArticleDto.Response> feedback(@PathVariable UUID id,
                                                       @RequestBody(required = false) java.util.Map<String, Object> body) {
        String tenantId = TenantContext.get();
        boolean helpful = body == null || !body.containsKey("helpful") || Boolean.TRUE.equals(body.get("helpful"));
        return ApiResponse.ok(kbArticleService.recordFeedback(tenantId, id, helpful));
    }

    private String getCurrentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
