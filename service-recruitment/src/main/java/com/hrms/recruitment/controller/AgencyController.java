package com.hrms.recruitment.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.recruitment.dto.AgencyDto;
import com.hrms.recruitment.service.AgencyService;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/recruitment/agencies")
@RequiredArgsConstructor
@Tag(name = "Recruitment Agencies", description = "Manage external recruitment agencies and their commission rates")
public class AgencyController {

    private final AgencyService agencyService;

    @PostMapping
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Register a new recruitment agency")
    public ResponseEntity<ApiResponse<AgencyDto.Response>> create(
            @Valid @RequestBody AgencyDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                agencyService.create(tenantId(), req, currentUserId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Update an agency's details")
    public ResponseEntity<ApiResponse<AgencyDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody AgencyDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                agencyService.update(tenantId(), id, req, currentUserId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "Get an agency by ID")
    public ResponseEntity<ApiResponse<AgencyDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(agencyService.get(tenantId(), id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "List all agencies (paginated, sorted by name)")
    public ResponseEntity<ApiResponse<List<AgencyDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AgencyDto.Response> page = agencyService.list(tenantId(), pageable);
        PaginationMeta meta = agencyService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    private String tenantId() { return TenantContext.get(); }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
