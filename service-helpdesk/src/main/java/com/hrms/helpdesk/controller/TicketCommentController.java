package com.hrms.helpdesk.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.helpdesk.dto.TicketCommentDto;
import com.hrms.helpdesk.service.TicketCommentService;
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
@RequestMapping("/api/v1/tickets/comments")
@RequiredArgsConstructor
public class TicketCommentController {

    private final TicketCommentService ticketCommentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('HELPDESK:WRITE')")
    public ApiResponse<TicketCommentDto.Response> create(
            @Valid @RequestBody TicketCommentDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = getCurrentUserId();
        return ApiResponse.ok(ticketCommentService.create(tenantId, currentUser, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('HELPDESK:READ')")
    public ApiResponse<TicketCommentDto.Response> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(ticketCommentService.getById(tenantId, id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('HELPDESK:READ')")
    public ApiResponse<Page<TicketCommentDto.Response>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(ticketCommentService.list(tenantId, pageable));
    }

    @GetMapping("/ticket/{ticketId}")
    @PreAuthorize("hasAuthority('HELPDESK:READ')")
    public ApiResponse<List<TicketCommentDto.Response>> listByTicket(@PathVariable UUID ticketId) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(ticketCommentService.listByTicket(tenantId, ticketId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HELPDESK:WRITE')")
    public ApiResponse<TicketCommentDto.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody TicketCommentDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = getCurrentUserId();
        return ApiResponse.ok(ticketCommentService.update(tenantId, id, currentUser, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HELPDESK:WRITE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = getCurrentUserId();
        ticketCommentService.delete(tenantId, id, currentUser);
        return ApiResponse.ok(null);
    }

    private String getCurrentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
