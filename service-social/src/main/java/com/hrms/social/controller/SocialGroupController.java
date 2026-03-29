package com.hrms.social.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import com.hrms.social.dto.SocialGroupDto;
import com.hrms.social.service.SocialGroupService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class SocialGroupController {

    private final SocialGroupService socialGroupService;

    @PostMapping
    @PreAuthorize("hasAuthority('SOCIAL:WRITE')")
    public ResponseEntity<ApiResponse<SocialGroupDto.Response>> create(
            @Valid @RequestBody SocialGroupDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        SocialGroupDto.Response response = socialGroupService.create(tenantId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SOCIAL:READ')")
    public ResponseEntity<ApiResponse<SocialGroupDto.Response>> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(socialGroupService.getById(tenantId, id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SOCIAL:READ')")
    public ResponseEntity<ApiResponse<Page<SocialGroupDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(socialGroupService.list(tenantId, pageable)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SOCIAL:WRITE')")
    public ResponseEntity<ApiResponse<SocialGroupDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody SocialGroupDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(socialGroupService.update(tenantId, id, request, currentUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SOCIAL:WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        socialGroupService.delete(tenantId, id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
