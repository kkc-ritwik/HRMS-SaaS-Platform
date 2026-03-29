package com.hrms.recruitment.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.recruitment.dto.JobRequisitionDto;
import com.hrms.recruitment.entity.JobRequisition;
import com.hrms.recruitment.service.JobRequisitionService;
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
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Job Requisitions", description = "Create, manage and approve job openings")
public class JobController {

    private final JobRequisitionService jobRequisitionService;

    @PostMapping
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Create a new job requisition (DRAFT)")
    public ResponseEntity<ApiResponse<JobRequisitionDto.Response>> create(
            @Valid @RequestBody JobRequisitionDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                jobRequisitionService.create(tenantId(), req, currentUserId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Update a job requisition")
    public ResponseEntity<ApiResponse<JobRequisitionDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody JobRequisitionDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobRequisitionService.update(tenantId(), id, req, currentUserId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "Get a job requisition by ID")
    public ResponseEntity<ApiResponse<JobRequisitionDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(jobRequisitionService.get(tenantId(), id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "List all job requisitions (paginated)")
    public ResponseEntity<ApiResponse<List<JobRequisitionDto.Response>>> list(
            @RequestParam(required = false) JobRequisition.RequisitionStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<JobRequisitionDto.Response> page = status != null
                ? jobRequisitionService.listByStatus(tenantId(), status, pageable)
                : jobRequisitionService.list(tenantId(), pageable);
        PaginationMeta meta = jobRequisitionService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    // ── Workflow actions ──────────────────────────────────────────────────────

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Submit a DRAFT requisition for approval")
    public ResponseEntity<ApiResponse<JobRequisitionDto.Response>> submit(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobRequisitionService.submit(tenantId(), id, currentUserId())));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('RECRUITMENT:APPROVE')")
    @Operation(summary = "Approve a PENDING_APPROVAL requisition")
    public ResponseEntity<ApiResponse<JobRequisitionDto.Response>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobRequisitionService.approve(tenantId(), id, currentUserId())));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Activate an APPROVED requisition (starts accepting applications)")
    public ResponseEntity<ApiResponse<JobRequisitionDto.Response>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobRequisitionService.activate(tenantId(), id, currentUserId())));
    }

    @PostMapping("/{id}/hold")
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Put an ACTIVE requisition ON_HOLD")
    public ResponseEntity<ApiResponse<JobRequisitionDto.Response>> hold(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobRequisitionService.hold(tenantId(), id, currentUserId())));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Close a requisition (all positions filled or abandoned)")
    public ResponseEntity<ApiResponse<JobRequisitionDto.Response>> close(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobRequisitionService.close(tenantId(), id, currentUserId())));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Cancel a requisition")
    public ResponseEntity<ApiResponse<JobRequisitionDto.Response>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobRequisitionService.cancel(tenantId(), id, currentUserId())));
    }

    private String tenantId() { return TenantContext.get(); }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
