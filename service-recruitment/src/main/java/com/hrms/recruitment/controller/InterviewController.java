package com.hrms.recruitment.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.recruitment.dto.InterviewDto;
import com.hrms.recruitment.entity.Interview;
import com.hrms.recruitment.service.InterviewService;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
@Tag(name = "Interviews", description = "Interview scheduling, panelist assignment and scorecard submission")
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Schedule an interview and assign panelists")
    public ResponseEntity<ApiResponse<InterviewDto.Response>> schedule(
            @Valid @RequestBody InterviewDto.ScheduleRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                interviewService.schedule(tenantId(), req, currentUserId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "Get an interview by ID (includes panelist scorecards)")
    public ResponseEntity<ApiResponse<InterviewDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(interviewService.get(tenantId(), id)));
    }

    @GetMapping("/application/{applicationId}")
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "Get all interviews for an application (ordered by round)")
    public ResponseEntity<ApiResponse<List<InterviewDto.Response>>> byApplication(
            @PathVariable UUID applicationId) {
        return ResponseEntity.ok(ApiResponse.ok(
                interviewService.listByApplication(tenantId(), applicationId)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Update interview status (COMPLETED / CANCELLED / NO_SHOW)")
    public ResponseEntity<ApiResponse<InterviewDto.Response>> updateStatus(
            @PathVariable UUID id,
            @RequestParam Interview.InterviewStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(
                interviewService.updateStatus(tenantId(), id, status, currentUserId())));
    }

    @PostMapping("/{id}/feedback")
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Submit overall interview feedback and mark as COMPLETED")
    public ResponseEntity<ApiResponse<InterviewDto.Response>> submitFeedback(
            @PathVariable UUID id,
            @Valid @RequestBody InterviewDto.FeedbackRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                interviewService.submitFeedback(tenantId(), id, req, currentUserId())));
    }

    // ── Panelist endpoints ────────────────────────────────────────────────────

    @PostMapping("/{id}/panelists")
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Add a panelist to an existing interview")
    public ResponseEntity<ApiResponse<InterviewDto.PanelistSummary>> addPanelist(
            @PathVariable UUID id,
            @Valid @RequestBody InterviewDto.PanelistRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                interviewService.addPanelist(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}/panelists/{panelistId}")
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Remove a panelist from an interview")
    public ResponseEntity<ApiResponse<Void>> removePanelist(
            @PathVariable UUID id,
            @PathVariable UUID panelistId) {
        interviewService.removePanelist(tenantId(), id, panelistId, currentUserId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{id}/panelists/{panelistId}/feedback")
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Panelist submits their individual scorecard")
    public ResponseEntity<ApiResponse<InterviewDto.PanelistSummary>> panelistFeedback(
            @PathVariable UUID id,
            @PathVariable UUID panelistId,
            @Valid @RequestBody InterviewDto.PanelistFeedbackRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                interviewService.submitPanelistFeedback(tenantId(), id, panelistId, req, currentUserId())));
    }

    private String tenantId() { return TenantContext.get(); }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
