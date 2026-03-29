package com.hrms.payroll.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.payroll.dto.TaxDeclarationDto;
import com.hrms.payroll.service.TaxDeclarationService;
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
@RequestMapping("/api/v1/tax/declarations")
@RequiredArgsConstructor
@Tag(name = "Tax Declarations", description = "Submit and manage tax declarations (IT regime, 80C/80D/HRA)")
public class TaxDeclarationController {

    private final TaxDeclarationService taxDeclarationService;

    // ── Employee self-service ─────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Submit or update my tax declaration for a financial year")
    public ResponseEntity<ApiResponse<TaxDeclarationDto.Response>> submit(
            @Valid @RequestBody TaxDeclarationDto.SubmitRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                taxDeclarationService.submit(tenantId(), employeeId(), req, currentUserId())));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "List my tax declarations")
    public ResponseEntity<ApiResponse<List<TaxDeclarationDto.Response>>> getMyDeclarations() {
        return ResponseEntity.ok(ApiResponse.ok(
                taxDeclarationService.getMyDeclarations(tenantId(), employeeId())));
    }

    @GetMapping("/me/{declarationId}")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "Get a specific declaration by ID")
    public ResponseEntity<ApiResponse<TaxDeclarationDto.Response>> getDeclaration(
            @PathVariable UUID declarationId) {
        return ResponseEntity.ok(ApiResponse.ok(
                taxDeclarationService.getDeclaration(tenantId(), declarationId)));
    }

    // ── Admin / payroll manager ───────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "List all SUBMITTED declarations for a financial year")
    public ResponseEntity<ApiResponse<List<TaxDeclarationDto.Response>>> listSubmitted(
            @RequestParam String financialYear) {
        return ResponseEntity.ok(ApiResponse.ok(
                taxDeclarationService.getSubmittedDeclarations(tenantId(), financialYear)));
    }

    @PostMapping("/{declarationId}/verify")
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Verify (approve) a submitted tax declaration")
    public ResponseEntity<ApiResponse<TaxDeclarationDto.Response>> verify(
            @PathVariable UUID declarationId) {
        return ResponseEntity.ok(ApiResponse.ok(
                taxDeclarationService.verify(tenantId(), declarationId, currentUserId())));
    }

    // ── Proof management ──────────────────────────────────────────────────────

    @PostMapping("/{declarationId}/proofs")
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Upload a proof document for a section")
    public ResponseEntity<ApiResponse<TaxDeclarationDto.ProofSummary>> addProof(
            @PathVariable UUID declarationId,
            @Valid @RequestBody TaxDeclarationDto.AddProofRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                taxDeclarationService.addProof(tenantId(), declarationId, req, currentUserId())));
    }

    @GetMapping("/{declarationId}/proofs")
    @PreAuthorize("hasAuthority('PAYROLL:READ')")
    @Operation(summary = "List proofs for a declaration")
    public ResponseEntity<ApiResponse<List<TaxDeclarationDto.ProofSummary>>> listProofs(
            @PathVariable UUID declarationId) {
        return ResponseEntity.ok(ApiResponse.ok(
                taxDeclarationService.getProofs(tenantId(), declarationId)));
    }

    @PostMapping("/{declarationId}/proofs/{proofId}/approve")
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Approve a proof document")
    public ResponseEntity<ApiResponse<TaxDeclarationDto.ProofSummary>> approveProof(
            @PathVariable UUID declarationId,
            @PathVariable UUID proofId) {
        return ResponseEntity.ok(ApiResponse.ok(
                taxDeclarationService.verifyProof(declarationId, proofId, true, currentUserId())));
    }

    @PostMapping("/{declarationId}/proofs/{proofId}/reject")
    @PreAuthorize("hasAuthority('PAYROLL:WRITE')")
    @Operation(summary = "Reject a proof document")
    public ResponseEntity<ApiResponse<TaxDeclarationDto.ProofSummary>> rejectProof(
            @PathVariable UUID declarationId,
            @PathVariable UUID proofId) {
        return ResponseEntity.ok(ApiResponse.ok(
                taxDeclarationService.verifyProof(declarationId, proofId, false, currentUserId())));
    }

    private String tenantId() { return TenantContext.get(); }

    private UUID employeeId() {
        return UUID.fromString(
                ((UserPrincipal) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal()).getEmployeeId());
    }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
