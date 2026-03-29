package com.hrms.payroll.service;

import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.payroll.dto.TaxDeclarationDto;
import com.hrms.payroll.entity.TaxDeclaration;
import com.hrms.payroll.entity.TaxProof;
import com.hrms.payroll.repository.TaxDeclarationRepository;
import com.hrms.payroll.repository.TaxProofRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaxDeclarationService {

    private final TaxDeclarationRepository declarationRepository;
    private final TaxProofRepository       proofRepository;

    // ── Submit / update ───────────────────────────────────────────────────────

    @Transactional
    public TaxDeclarationDto.Response submit(String tenantId, UUID employeeId,
                                             TaxDeclarationDto.SubmitRequest req,
                                             String currentUser) {
        // Upsert: one declaration per employee per FY
        TaxDeclaration decl = declarationRepository
                .findByEmployeeIdAndTenantIdAndFinancialYearAndDeletedFalse(
                        employeeId, tenantId, req.getFinancialYear())
                .orElseGet(() -> {
                    TaxDeclaration d = new TaxDeclaration();
                    d.setTenantId(tenantId);
                    d.setEmployeeId(employeeId);
                    d.setFinancialYear(req.getFinancialYear());
                    d.setCreatedBy(currentUser);
                    return d;
                });

        if (decl.getStatus() == TaxDeclaration.DeclarationStatus.VERIFIED) {
            throw new BusinessException("DECLARATION_LOCKED",
                    "Verified declarations cannot be modified. Contact payroll admin.");
        }

        decl.setRegime(req.getRegime());
        decl.setSection80c(req.getSection80c());
        decl.setSection80d(req.getSection80d());
        decl.setSection80e(req.getSection80e());
        decl.setSection80g(req.getSection80g());
        decl.setSection24b(req.getSection24b());
        decl.setHraExemptionClaimed(req.getHraExemptionClaimed());
        decl.setLtaClaimed(req.getLtaClaimed());
        decl.setOtherIncome(req.getOtherIncome());
        decl.setPreviousEmployerIncome(req.getPreviousEmployerIncome());
        decl.setPreviousEmployerTds(req.getPreviousEmployerTds());
        decl.setStatus(TaxDeclaration.DeclarationStatus.SUBMITTED);
        decl.setUpdatedBy(currentUser);

        TaxDeclaration saved = declarationRepository.save(decl);
        return toResponse(saved);
    }

    // ── Admin verify ──────────────────────────────────────────────────────────

    @Transactional
    public TaxDeclarationDto.Response verify(String tenantId, UUID declarationId, String currentUser) {
        TaxDeclaration decl = getEntity(tenantId, declarationId);
        if (decl.getStatus() != TaxDeclaration.DeclarationStatus.SUBMITTED) {
            throw new BusinessException("INVALID_STATUS",
                    "Only SUBMITTED declarations can be verified. Current: " + decl.getStatus());
        }
        decl.setStatus(TaxDeclaration.DeclarationStatus.VERIFIED);
        decl.setUpdatedBy(currentUser);
        return toResponse(declarationRepository.save(decl));
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TaxDeclarationDto.Response> getMyDeclarations(String tenantId, UUID employeeId) {
        return declarationRepository
                .findByEmployeeIdAndTenantIdAndDeletedFalseOrderByFinancialYearDesc(employeeId, tenantId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TaxDeclarationDto.Response getDeclaration(String tenantId, UUID declarationId) {
        return toResponse(getEntity(tenantId, declarationId));
    }

    @Transactional(readOnly = true)
    public List<TaxDeclarationDto.Response> getSubmittedDeclarations(
            String tenantId, String financialYear) {
        return declarationRepository
                .findByTenantIdAndFinancialYearAndStatusAndDeletedFalse(
                        tenantId, financialYear, TaxDeclaration.DeclarationStatus.SUBMITTED)
                .stream().map(this::toResponse).toList();
    }

    // ── Proof management ─────────────────────────────────────────────────────

    @Transactional
    public TaxDeclarationDto.ProofSummary addProof(String tenantId, UUID declarationId,
                                                    TaxDeclarationDto.AddProofRequest req,
                                                    String currentUser) {
        TaxDeclaration decl = getEntity(tenantId, declarationId);
        if (decl.getStatus() == TaxDeclaration.DeclarationStatus.VERIFIED) {
            throw new BusinessException("DECLARATION_LOCKED",
                    "Cannot add proofs to a verified declaration.");
        }

        TaxProof proof = new TaxProof();
        proof.setDeclarationId(decl.getId());
        proof.setSection(req.getSection());
        proof.setDescription(req.getDescription());
        proof.setDeclaredAmount(req.getDeclaredAmount());
        proof.setProofAmount(req.getProofAmount());
        proof.setProofUrl(req.getProofUrl());
        proof.setStatus(TaxProof.ProofStatus.PENDING);

        return toProofSummary(proofRepository.save(proof));
    }

    @Transactional
    public TaxDeclarationDto.ProofSummary verifyProof(UUID declarationId, UUID proofId,
                                                       boolean approve, String currentUser) {
        TaxProof proof = proofRepository.findByIdAndDeclarationId(proofId, declarationId)
                .orElseThrow(() -> new ResourceNotFoundException("TaxProof", "id", proofId));
        proof.setStatus(approve ? TaxProof.ProofStatus.APPROVED : TaxProof.ProofStatus.REJECTED);
        proof.setVerifiedBy(currentUser);
        return toProofSummary(proofRepository.save(proof));
    }

    @Transactional(readOnly = true)
    public List<TaxDeclarationDto.ProofSummary> getProofs(String tenantId, UUID declarationId) {
        getEntity(tenantId, declarationId); // security: verify tenant owns declaration
        return proofRepository.findByDeclarationId(declarationId)
                .stream().map(this::toProofSummary).toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private TaxDeclaration getEntity(String tenantId, UUID id) {
        return declarationRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("TaxDeclaration", "id", id));
    }

    private TaxDeclarationDto.Response toResponse(TaxDeclaration d) {
        List<TaxDeclarationDto.ProofSummary> proofs = proofRepository
                .findByDeclarationId(d.getId())
                .stream().map(this::toProofSummary).toList();

        return TaxDeclarationDto.Response.builder()
                .id(d.getId())
                .employeeId(d.getEmployeeId())
                .financialYear(d.getFinancialYear())
                .regime(d.getRegime())
                .status(d.getStatus())
                .section80c(d.getSection80c())
                .section80d(d.getSection80d())
                .section80e(d.getSection80e())
                .section80g(d.getSection80g())
                .section24b(d.getSection24b())
                .hraExemptionClaimed(d.getHraExemptionClaimed())
                .ltaClaimed(d.getLtaClaimed())
                .otherIncome(d.getOtherIncome())
                .previousEmployerIncome(d.getPreviousEmployerIncome())
                .previousEmployerTds(d.getPreviousEmployerTds())
                .proofs(proofs)
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private TaxDeclarationDto.ProofSummary toProofSummary(TaxProof p) {
        return TaxDeclarationDto.ProofSummary.builder()
                .id(p.getId())
                .section(p.getSection())
                .description(p.getDescription())
                .declaredAmount(p.getDeclaredAmount())
                .proofAmount(p.getProofAmount())
                .proofUrl(p.getProofUrl())
                .status(p.getStatus())
                .verifiedBy(p.getVerifiedBy())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
