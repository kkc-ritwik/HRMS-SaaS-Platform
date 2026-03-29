package com.hrms.helpdesk.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.helpdesk.dto.TicketCategoryDto;
import com.hrms.helpdesk.service.TicketCategoryService;
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
@RequestMapping("/api/v1/tickets/categories")
@RequiredArgsConstructor
public class TicketCategoryController {

    private final TicketCategoryService ticketCategoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('HELPDESK:WRITE')")
    public ApiResponse<TicketCategoryDto.Response> create(
            @Valid @RequestBody TicketCategoryDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = getCurrentUserId();
        return ApiResponse.ok(ticketCategoryService.create(tenantId, currentUser, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('HELPDESK:READ')")
    public ApiResponse<TicketCategoryDto.Response> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(ticketCategoryService.getById(tenantId, id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('HELPDESK:READ')")
    public ApiResponse<Page<TicketCategoryDto.Response>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(ticketCategoryService.list(tenantId, pageable));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('HELPDESK:READ')")
    public ApiResponse<List<TicketCategoryDto.Response>> listAll() {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(ticketCategoryService.listAll(tenantId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HELPDESK:WRITE')")
    public ApiResponse<TicketCategoryDto.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody TicketCategoryDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = getCurrentUserId();
        return ApiResponse.ok(ticketCategoryService.update(tenantId, id, currentUser, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HELPDESK:WRITE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = getCurrentUserId();
        ticketCategoryService.delete(tenantId, id, currentUser);
        return ApiResponse.ok(null);
    }

    private String getCurrentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
