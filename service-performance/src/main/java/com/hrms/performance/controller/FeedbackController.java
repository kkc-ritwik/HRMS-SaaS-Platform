package com.hrms.performance.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.performance.dto.FeedbackDto;
import com.hrms.performance.service.FeedbackService;
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
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
@Tag(name = "Feedback", description = "Continuous feedback: appreciation, constructive, and improvement feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    @PreAuthorize("hasAuthority('PERFORMANCE:WRITE')")
    @Operation(summary = "Give feedback to an employee (supports anonymous feedback)")
    public ResponseEntity<ApiResponse<FeedbackDto.Response>> give(
            @Valid @RequestBody FeedbackDto.CreateRequest req) {
        UUID fromEmployeeId = UUID.fromString(principal().getEmployeeId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(feedbackService.give(tenantId(), fromEmployeeId, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFORMANCE:WRITE')")
    @Operation(summary = "Soft-delete a feedback item (only the giver can delete)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        feedbackService.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "Get a feedback item by ID")
    public ResponseEntity<ApiResponse<FeedbackDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(feedbackService.get(tenantId(), id)));
    }

    // ── Received (self-service) ───────────────────────────────────────────────

    @GetMapping("/me/received")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "My received feedback (visibility-filtered; managers see MANAGER_ONLY items too)")
    public ResponseEntity<ApiResponse<List<FeedbackDto.Response>>> myReceived(
            @RequestParam(defaultValue = "false") boolean isManager,
            @PageableDefault(size = 20) Pageable pageable) {
        UUID employeeId = UUID.fromString(principal().getEmployeeId());
        Page<FeedbackDto.Response> page = feedbackService.received(tenantId(), employeeId, isManager, pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), feedbackService.buildMeta(page)));
    }

    @GetMapping("/employee/{employeeId}/received")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "Received feedback for a specific employee (HR/manager use)")
    public ResponseEntity<ApiResponse<List<FeedbackDto.Response>>> receivedForEmployee(
            @PathVariable UUID employeeId,
            @RequestParam(defaultValue = "true") boolean isManager,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<FeedbackDto.Response> page = feedbackService.received(tenantId(), employeeId, isManager, pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), feedbackService.buildMeta(page)));
    }

    // ── Given (self-service) ──────────────────────────────────────────────────

    @GetMapping("/me/given")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "Feedback I have given to others")
    public ResponseEntity<ApiResponse<List<FeedbackDto.Response>>> myGiven(
            @PageableDefault(size = 20) Pageable pageable) {
        UUID employeeId = UUID.fromString(principal().getEmployeeId());
        Page<FeedbackDto.Response> page = feedbackService.given(tenantId(), employeeId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), feedbackService.buildMeta(page)));
    }

    // ── Public wall ───────────────────────────────────────────────────────────

    @GetMapping("/wall")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "Public appreciation wall — PUBLIC visibility appreciation feedback")
    public ResponseEntity<ApiResponse<List<FeedbackDto.Response>>> publicWall(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<FeedbackDto.Response> page = feedbackService.publicWall(tenantId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), feedbackService.buildMeta(page)));
    }

    private String tenantId() { return TenantContext.get(); }

    private UserPrincipal principal() {
        return (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    private String currentUserId() { return principal().getId(); }
}
