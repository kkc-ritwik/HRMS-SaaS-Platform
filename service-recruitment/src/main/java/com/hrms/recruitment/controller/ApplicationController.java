package com.hrms.recruitment.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.recruitment.dto.ApplicationDto;
import com.hrms.recruitment.entity.Application;
import com.hrms.recruitment.service.ApplicationService;
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
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Applications", description = "Application pipeline management and stage transitions")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Submit a new application (candidate applies to a requisition)")
    public ResponseEntity<ApiResponse<ApplicationDto.Response>> apply(
            @Valid @RequestBody ApplicationDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                applicationService.apply(tenantId(), req, currentUserId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "Get an application by ID")
    public ResponseEntity<ApiResponse<ApplicationDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(applicationService.get(tenantId(), id)));
    }

    @GetMapping("/job/{requisitionId}")
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "List all applications for a job requisition (paginated)")
    public ResponseEntity<ApiResponse<List<ApplicationDto.Response>>> byJob(
            @PathVariable UUID requisitionId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ApplicationDto.Response> page =
                applicationService.listByRequisition(tenantId(), requisitionId, pageable);
        PaginationMeta meta = applicationService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/candidate/{candidateId}")
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "List all applications for a candidate (paginated)")
    public ResponseEntity<ApiResponse<List<ApplicationDto.Response>>> byCandidate(
            @PathVariable UUID candidateId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ApplicationDto.Response> page =
                applicationService.listByCandidate(tenantId(), candidateId, pageable);
        PaginationMeta meta = applicationService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/stage/{stage}")
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "List all applications in a specific pipeline stage (paginated)")
    public ResponseEntity<ApiResponse<List<ApplicationDto.Response>>> byStage(
            @PathVariable Application.ApplicationStage stage,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ApplicationDto.Response> page =
                applicationService.listByStage(tenantId(), stage, pageable);
        PaginationMeta meta = applicationService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/job/{requisitionId}/pipeline")
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "Get stage-wise application counts for a requisition (funnel view)")
    public ResponseEntity<ApiResponse<List<ApplicationDto.StageCount>>> pipeline(
            @PathVariable UUID requisitionId) {
        return ResponseEntity.ok(ApiResponse.ok(
                applicationService.getPipelineCounts(tenantId(), requisitionId)));
    }

    @PostMapping("/{id}/move")
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Move an application to the next pipeline stage")
    public ResponseEntity<ApiResponse<ApplicationDto.Response>> move(
            @PathVariable UUID id,
            @Valid @RequestBody ApplicationDto.MoveStageRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                applicationService.moveStage(tenantId(), id, req, currentUserId())));
    }

    private String tenantId() { return TenantContext.get(); }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
