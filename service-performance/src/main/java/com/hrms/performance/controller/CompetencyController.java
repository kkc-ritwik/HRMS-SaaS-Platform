package com.hrms.performance.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.performance.dto.CompetencyDto;
import com.hrms.performance.service.CompetencyService;
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
@RequestMapping("/api/v1/competencies")
@RequiredArgsConstructor
@Tag(name = "Competencies", description = "Competency framework management and role-competency mappings")
public class CompetencyController {

    private final CompetencyService competencyService;

    @PostMapping
    @PreAuthorize("hasAuthority('PERFORMANCE:ADMIN')")
    @Operation(summary = "Create a new competency")
    public ResponseEntity<ApiResponse<CompetencyDto.Response>> create(
            @Valid @RequestBody CompetencyDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(competencyService.create(tenantId(), req, currentUserId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFORMANCE:ADMIN')")
    @Operation(summary = "Update a competency")
    public ResponseEntity<ApiResponse<CompetencyDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CompetencyDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                competencyService.update(tenantId(), id, req, currentUserId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "Get a competency by ID")
    public ResponseEntity<ApiResponse<CompetencyDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(competencyService.get(tenantId(), id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "List all competencies (paginated)")
    public ResponseEntity<ApiResponse<List<CompetencyDto.Response>>> list(
            @PageableDefault(size = 50) Pageable pageable) {
        Page<CompetencyDto.Response> page = competencyService.list(tenantId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), competencyService.buildMeta(page)));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "List all active competencies (for review forms)")
    public ResponseEntity<ApiResponse<List<CompetencyDto.Response>>> listActive() {
        return ResponseEntity.ok(ApiResponse.ok(competencyService.listActive(tenantId())));
    }

    // ── Role mappings ─────────────────────────────────────────────────────────

    @PostMapping("/role-mappings")
    @PreAuthorize("hasAuthority('PERFORMANCE:ADMIN')")
    @Operation(summary = "Map a competency to a role or department")
    public ResponseEntity<ApiResponse<CompetencyDto.RoleMappingResponse>> addRoleMapping(
            @Valid @RequestBody CompetencyDto.RoleMappingRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(competencyService.addRoleMapping(tenantId(), req, currentUserId())));
    }

    @DeleteMapping("/role-mappings/{mappingId}")
    @PreAuthorize("hasAuthority('PERFORMANCE:ADMIN')")
    @Operation(summary = "Remove a competency-to-role mapping")
    public ResponseEntity<Void> removeRoleMapping(@PathVariable UUID mappingId) {
        competencyService.removeRoleMapping(tenantId(), mappingId, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/role-mappings/role/{roleId}")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "Get all competency mappings for a given role")
    public ResponseEntity<ApiResponse<List<CompetencyDto.RoleMappingResponse>>> getMappingsForRole(
            @PathVariable UUID roleId) {
        return ResponseEntity.ok(ApiResponse.ok(
                competencyService.getMappingsForRole(tenantId(), roleId)));
    }

    private String tenantId() { return TenantContext.get(); }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
