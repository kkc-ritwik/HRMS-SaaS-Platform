package com.hrms.notification.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.notification.dto.AnnouncementDto;
import com.hrms.notification.service.AnnouncementService;
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
@RequestMapping("/api/v1/notifications/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping
    @PreAuthorize("hasAuthority('NOTIFICATION:WRITE')")
    public ResponseEntity<ApiResponse<AnnouncementDto.Response>> create(
            @Valid @RequestBody AnnouncementDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        AnnouncementDto.Response response = announcementService.create(tenantId, currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION:READ')")
    public ResponseEntity<ApiResponse<AnnouncementDto.Response>> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        AnnouncementDto.Response response = announcementService.getById(tenantId, id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('NOTIFICATION:READ')")
    public ResponseEntity<ApiResponse<List<AnnouncementDto.Response>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String tenantId = TenantContext.get();
        Pageable pageable = PageRequest.of(page, size);
        Page<AnnouncementDto.Response> result = announcementService.list(tenantId, pageable);
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
    public ResponseEntity<ApiResponse<AnnouncementDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody AnnouncementDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        AnnouncementDto.Response response =
                announcementService.update(tenantId, id, currentUser, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION:WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        announcementService.delete(tenantId, id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
