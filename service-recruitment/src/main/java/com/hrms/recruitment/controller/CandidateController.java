package com.hrms.recruitment.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.recruitment.dto.CandidateDto;
import com.hrms.recruitment.service.CandidateService;
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
@RequestMapping("/api/v1/candidates")
@RequiredArgsConstructor
@Tag(name = "Candidates", description = "Candidate profile management and resume tracking")
public class CandidateController {

    private final CandidateService candidateService;

    @PostMapping
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Create a new candidate profile")
    public ResponseEntity<ApiResponse<CandidateDto.Response>> create(
            @Valid @RequestBody CandidateDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                candidateService.create(tenantId(), req, currentUserId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Update a candidate profile")
    public ResponseEntity<ApiResponse<CandidateDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CandidateDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                candidateService.update(tenantId(), id, req, currentUserId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "Get a candidate by ID")
    public ResponseEntity<ApiResponse<CandidateDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(candidateService.get(tenantId(), id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "List or search candidates (paginated). Use ?q= for name/email search.")
    public ResponseEntity<ApiResponse<List<CandidateDto.Response>>> list(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CandidateDto.Response> page = (q != null && !q.isBlank())
                ? candidateService.search(tenantId(), q, pageable)
                : candidateService.list(tenantId(), pageable);
        PaginationMeta meta = candidateService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Soft-delete a candidate")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        candidateService.delete(tenantId(), id, currentUserId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    private String tenantId() { return TenantContext.get(); }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
