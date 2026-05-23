package com.hrms.payroll.controller;

import com.hrms.payroll.entity.InvestmentProof;
import com.hrms.security.model.TenantContext;
import com.hrms.storage.model.StoredFile;
import com.hrms.storage.security.AntivirusScanner;
import com.hrms.storage.security.FileSecurityValidator;
import com.hrms.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Submit & verify investment proofs for the current FY.
 *   POST   /api/payroll/investment-proofs        — multipart upload (file + metadata)
 *   GET    /api/payroll/investment-proofs/me     — employee's submissions for FY
 *   GET    /api/payroll/investment-proofs/queue  — finance reviewer queue (UPLOADED/UNDER_REVIEW)
 *   POST   /api/payroll/investment-proofs/{id}/verify
 *   POST   /api/payroll/investment-proofs/{id}/reject
 */
@RestController
@RequestMapping("/api/payroll/investment-proofs")
@RequiredArgsConstructor
public class InvestmentProofController {

    public interface Repo extends JpaRepository<InvestmentProof, UUID> {
        @Query("SELECT p FROM InvestmentProof p WHERE p.tenantId = :t AND p.employeeId = :e AND p.financialYear = :fy ORDER BY p.createdAt DESC")
        List<InvestmentProof> ofEmployee(@Param("t") String tenant, @Param("e") UUID emp, @Param("fy") String fy);

        @Query("SELECT p FROM InvestmentProof p WHERE p.tenantId = :t AND p.status IN ('UPLOADED','UNDER_REVIEW') ORDER BY p.createdAt")
        List<InvestmentProof> reviewQueue(@Param("t") String tenant);
    }

    private final Repo repo;
    private final StorageService storage;
    private final FileSecurityValidator validator;
    private final AntivirusScanner antivirus;

    @PostMapping
    @Transactional
    public ResponseEntity<InvestmentProof> upload(@RequestPart("file") MultipartFile file,
                                                  @RequestPart("meta") Map<String, Object> meta) throws IOException {
        validator.validate(file);
        AntivirusScanner.ScanResult av = antivirus.scan(file);
        if (av.isInfected()) {
            throw new IllegalArgumentException("File rejected by antivirus: " + av.detail());
        }

        String tenant = TenantContext.get();
        UUID employeeId = UUID.fromString((String) meta.get("employeeId"));
        String fy = (String) meta.get("financialYear");

        String filename = validator.sanitizeFilename(file.getOriginalFilename());
        StoredFile sf = storage.upload(tenant,
                "tax-proofs/" + fy + "/" + employeeId,
                filename, file.getContentType(), file.getInputStream(), file.getSize());

        InvestmentProof p = new InvestmentProof();
        p.setTenantId(tenant);
        p.setEmployeeId(employeeId);
        if (meta.get("declarationId") != null) p.setDeclarationId(UUID.fromString((String) meta.get("declarationId")));
        p.setFinancialYear(fy);
        p.setSection(InvestmentProof.Section.valueOf((String) meta.get("section")));
        p.setSubSection((String) meta.get("subSection"));
        p.setInstrumentType((String) meta.get("instrumentType"));
        p.setClaimedAmount(new BigDecimal(meta.get("claimedAmount").toString()));
        p.setDocumentUri(sf.getStorageUri());
        p.setDocumentType((String) meta.get("documentType"));
        if (meta.get("documentDate") != null) p.setDocumentDate(LocalDate.parse((String) meta.get("documentDate")));
        p.setPolicyNumber((String) meta.get("policyNumber"));
        p.setPanNumber((String) meta.get("panNumber"));
        p.setBankAccount((String) meta.get("bankAccount"));
        p.setNotes((String) meta.get("notes"));
        return ResponseEntity.ok(repo.save(p));
    }

    @GetMapping("/me")
    public List<InvestmentProof> mine(@RequestParam UUID employeeId, @RequestParam String financialYear) {
        return repo.ofEmployee(TenantContext.get(), employeeId, financialYear);
    }

    @GetMapping("/queue")
    public List<InvestmentProof> queue() {
        return repo.reviewQueue(TenantContext.get());
    }

    @PostMapping("/{id}/verify")
    @Transactional
    public InvestmentProof verify(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        InvestmentProof p = repo.findById(id).orElseThrow();
        p.setStatus(InvestmentProof.Status.ACCEPTED);
        if (body.get("verifiedAmount") != null) {
            p.setVerifiedAmount(new BigDecimal(body.get("verifiedAmount").toString()));
        } else {
            p.setVerifiedAmount(p.getClaimedAmount());
        }
        if (body.get("verifiedBy") != null) p.setVerifiedBy(UUID.fromString((String) body.get("verifiedBy")));
        p.setVerifiedAt(OffsetDateTime.now());
        p.setReviewerComment((String) body.get("comment"));
        return repo.save(p);
    }

    @PostMapping("/{id}/reject")
    @Transactional
    public InvestmentProof reject(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        InvestmentProof p = repo.findById(id).orElseThrow();
        p.setStatus(InvestmentProof.Status.REJECTED);
        p.setVerifiedAt(OffsetDateTime.now());
        if (body.get("verifiedBy") != null) p.setVerifiedBy(UUID.fromString((String) body.get("verifiedBy")));
        p.setReviewerComment((String) body.get("comment"));
        return repo.save(p);
    }
}
