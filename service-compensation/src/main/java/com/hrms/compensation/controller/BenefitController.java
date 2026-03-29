package com.hrms.compensation.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.compensation.dto.BenefitDto;
import com.hrms.compensation.entity.Benefit.BenefitType;
import com.hrms.compensation.service.BenefitService;
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
@RequestMapping("/api/v1/compensation/benefits")
@RequiredArgsConstructor
public class BenefitController {

    private final BenefitService benefitService;

    @PostMapping
    @PreAuthorize("hasAuthority('COMPENSATION:WRITE')")
    public ResponseEntity<ApiResponse<BenefitDto.Response>> create(
            @Valid @RequestBody BenefitDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        BenefitDto.Response response = benefitService.create(tenantId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPENSATION:READ')")
    public ResponseEntity<ApiResponse<BenefitDto.Response>> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(benefitService.getById(tenantId, id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COMPENSATION:READ')")
    public ResponseEntity<ApiResponse<Page<BenefitDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(benefitService.list(tenantId, pageable)));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('COMPENSATION:READ')")
    public ResponseEntity<ApiResponse<List<BenefitDto.Response>>> listActive() {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(benefitService.listActive(tenantId)));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAuthority('COMPENSATION:READ')")
    public ResponseEntity<ApiResponse<List<BenefitDto.Response>>> listByType(
            @PathVariable BenefitType type) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(benefitService.listByType(tenantId, type)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPENSATION:WRITE')")
    public ResponseEntity<ApiResponse<BenefitDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody BenefitDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(benefitService.update(tenantId, id, request, currentUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPENSATION:WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        benefitService.delete(tenantId, id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
