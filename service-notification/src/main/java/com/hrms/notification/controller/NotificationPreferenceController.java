package com.hrms.notification.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.notification.dto.NotificationPreferenceDto;
import com.hrms.notification.service.NotificationPreferenceService;
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
@RequestMapping("/api/v1/notifications/preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final NotificationPreferenceService notificationPreferenceService;

    @PostMapping
    @PreAuthorize("hasAuthority('NOTIFICATION:WRITE')")
    public ResponseEntity<ApiResponse<NotificationPreferenceDto.Response>> create(
            @Valid @RequestBody NotificationPreferenceDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        NotificationPreferenceDto.Response response =
                notificationPreferenceService.create(tenantId, currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION:READ')")
    public ResponseEntity<ApiResponse<NotificationPreferenceDto.Response>> getById(
            @PathVariable UUID id) {
        String tenantId = TenantContext.get();
        NotificationPreferenceDto.Response response =
                notificationPreferenceService.getById(tenantId, id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('NOTIFICATION:READ')")
    public ResponseEntity<ApiResponse<List<NotificationPreferenceDto.Response>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String tenantId = TenantContext.get();
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationPreferenceDto.Response> result =
                notificationPreferenceService.list(tenantId, pageable);
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

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('NOTIFICATION:READ')")
    public ResponseEntity<ApiResponse<List<NotificationPreferenceDto.Response>>> listForEmployee(
            @PathVariable UUID employeeId) {
        String tenantId = TenantContext.get();
        List<NotificationPreferenceDto.Response> response =
                notificationPreferenceService.listForEmployee(tenantId, employeeId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION:WRITE')")
    public ResponseEntity<ApiResponse<NotificationPreferenceDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody NotificationPreferenceDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        NotificationPreferenceDto.Response response =
                notificationPreferenceService.update(tenantId, id, currentUser, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION:WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        notificationPreferenceService.delete(tenantId, id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
