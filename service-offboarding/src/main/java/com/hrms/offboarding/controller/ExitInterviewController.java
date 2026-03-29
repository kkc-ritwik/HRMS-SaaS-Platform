package com.hrms.offboarding.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.offboarding.dto.ExitInterviewDto;
import com.hrms.offboarding.service.ExitInterviewService;
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
@RequestMapping("/api/v1/offboarding/exit-interviews")
@RequiredArgsConstructor
@Tag(name = "Exit Interviews", description = "Manage exit interviews")
public class ExitInterviewController {

    private final ExitInterviewService exitInterviewService;

    @PostMapping
    @PreAuthorize("hasAuthority('OFFBOARDING:WRITE')")
    @Operation(summary = "Schedule an exit interview")
    public ResponseEntity<ApiResponse<ExitInterviewDto.Response>> create(
            @Valid @RequestBody ExitInterviewDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(exitInterviewService.create(tenantId(), req, currentUserId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('OFFBOARDING:READ')")
    @Operation(summary = "Get an exit interview by ID")
    public ResponseEntity<ApiResponse<ExitInterviewDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(exitInterviewService.get(tenantId(), id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('OFFBOARDING:READ')")
    @Operation(summary = "List all exit interviews with pagination")
    public ResponseEntity<ApiResponse<List<ExitInterviewDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ExitInterviewDto.Response> page = exitInterviewService.list(tenantId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), exitInterviewService.buildMeta(page)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('OFFBOARDING:WRITE')")
    @Operation(summary = "Update an exit interview")
    public ResponseEntity<ApiResponse<ExitInterviewDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ExitInterviewDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(exitInterviewService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('OFFBOARDING:WRITE')")
    @Operation(summary = "Soft-delete an exit interview")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        exitInterviewService.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/separation/{separationId}")
    @PreAuthorize("hasAuthority('OFFBOARDING:READ')")
    @Operation(summary = "List exit interviews by separation")
    public ResponseEntity<ApiResponse<List<ExitInterviewDto.Response>>> listBySeparation(
            @PathVariable UUID separationId) {
        return ResponseEntity.ok(ApiResponse.ok(exitInterviewService.listBySeparation(tenantId(), separationId)));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('OFFBOARDING:WRITE')")
    @Operation(summary = "Mark an exit interview as completed")
    public ResponseEntity<ApiResponse<ExitInterviewDto.Response>> complete(
            @PathVariable UUID id,
            @RequestBody ExitInterviewDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(exitInterviewService.complete(tenantId(), id, req, currentUserId())));
    }

    private String tenantId() {
        return TenantContext.get();
    }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}
