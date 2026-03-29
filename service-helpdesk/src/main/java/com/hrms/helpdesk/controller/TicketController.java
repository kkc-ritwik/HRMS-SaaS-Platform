package com.hrms.helpdesk.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.helpdesk.dto.TicketDto;
import com.hrms.helpdesk.entity.Ticket.TicketStatus;
import com.hrms.helpdesk.service.TicketService;
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
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('HELPDESK:WRITE')")
    public ApiResponse<TicketDto.Response> create(
            @Valid @RequestBody TicketDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = getCurrentUserId();
        return ApiResponse.ok(ticketService.create(tenantId, currentUser, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('HELPDESK:READ')")
    public ApiResponse<TicketDto.Response> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(ticketService.getById(tenantId, id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('HELPDESK:READ')")
    public ApiResponse<Page<TicketDto.Response>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(ticketService.list(tenantId, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HELPDESK:WRITE')")
    public ApiResponse<TicketDto.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody TicketDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = getCurrentUserId();
        return ApiResponse.ok(ticketService.update(tenantId, id, currentUser, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HELPDESK:WRITE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = getCurrentUserId();
        ticketService.delete(tenantId, id, currentUser);
        return ApiResponse.ok(null);
    }

    @GetMapping("/requester/{requesterId}")
    @PreAuthorize("hasAuthority('HELPDESK:READ')")
    public ApiResponse<Page<TicketDto.Response>> listByRequester(
            @PathVariable UUID requesterId,
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(ticketService.listByRequester(tenantId, requesterId, pageable));
    }

    @GetMapping("/assignee/{assigneeId}")
    @PreAuthorize("hasAuthority('HELPDESK:READ')")
    public ApiResponse<Page<TicketDto.Response>> listByAssignee(
            @PathVariable UUID assigneeId,
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(ticketService.listByAssignee(tenantId, assigneeId, pageable));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('HELPDESK:READ')")
    public ApiResponse<Page<TicketDto.Response>> listByStatus(
            @PathVariable TicketStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(ticketService.listByStatus(tenantId, status, pageable));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('HELPDESK:WRITE')")
    public ApiResponse<TicketDto.Response> resolve(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = getCurrentUserId();
        return ApiResponse.ok(ticketService.resolve(tenantId, id, currentUser));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('HELPDESK:WRITE')")
    public ApiResponse<TicketDto.Response> close(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = getCurrentUserId();
        return ApiResponse.ok(ticketService.close(tenantId, id, currentUser));
    }

    private String getCurrentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
