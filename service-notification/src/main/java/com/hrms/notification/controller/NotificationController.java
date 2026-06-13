package com.hrms.notification.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.notification.dto.NotificationDto;
import com.hrms.notification.service.NotificationService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasAuthority('NOTIFICATION:WRITE')")
    public ResponseEntity<ApiResponse<NotificationDto.Response>> create(
            @Valid @RequestBody NotificationDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        NotificationDto.Response response = notificationService.create(tenantId, currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION:READ')")
    public ResponseEntity<ApiResponse<NotificationDto.Response>> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        NotificationDto.Response response = notificationService.getById(tenantId, id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('NOTIFICATION:READ')")
    public ResponseEntity<ApiResponse<java.util.List<NotificationDto.Response>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String tenantId = TenantContext.get();
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto.Response> result = notificationService.list(tenantId, pageable);
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
    public ResponseEntity<ApiResponse<java.util.List<NotificationDto.Response>>> listForEmployee(
            @PathVariable UUID employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String tenantId = TenantContext.get();
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto.Response> result =
                notificationService.listForEmployee(tenantId, employeeId, pageable);
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

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION:WRITE')")
    public ResponseEntity<ApiResponse<NotificationDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody NotificationDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        NotificationDto.Response response = notificationService.update(tenantId, id, currentUser, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("hasAuthority('NOTIFICATION:WRITE')")
    public ResponseEntity<ApiResponse<NotificationDto.Response>> markRead(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        NotificationDto.Response response = notificationService.markRead(tenantId, id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/read-all")
    @PreAuthorize("hasAuthority('NOTIFICATION:WRITE')")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> markAllRead() {
        String tenantId = TenantContext.get();
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        UUID employeeId = UUID.fromString(principal.getEmployeeId());
        int updated = notificationService.markAllRead(tenantId, employeeId, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(java.util.Map.of("markedRead", updated)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION:WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        notificationService.delete(tenantId, id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
