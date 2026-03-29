package com.hrms.compensation.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.compensation.dto.PayGradeDto;
import com.hrms.compensation.service.PayGradeService;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
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
@RequestMapping("/api/v1/compensation/pay-grades")
@RequiredArgsConstructor
public class PayGradeController {

    private final PayGradeService payGradeService;

    @PostMapping
    @PreAuthorize("hasAuthority('COMPENSATION:WRITE')")
    public ResponseEntity<ApiResponse<PayGradeDto.Response>> create(
            @Valid @RequestBody PayGradeDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        PayGradeDto.Response response = payGradeService.create(tenantId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPENSATION:READ')")
    public ResponseEntity<ApiResponse<PayGradeDto.Response>> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(payGradeService.getById(tenantId, id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COMPENSATION:READ')")
    public ResponseEntity<ApiResponse<Page<PayGradeDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(payGradeService.list(tenantId, pageable)));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('COMPENSATION:READ')")
    public ResponseEntity<ApiResponse<List<PayGradeDto.Response>>> listActive() {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(payGradeService.listActive(tenantId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPENSATION:WRITE')")
    public ResponseEntity<ApiResponse<PayGradeDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody PayGradeDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(payGradeService.update(tenantId, id, request, currentUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPENSATION:WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        payGradeService.delete(tenantId, id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
