package com.hrms.recruitment.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.recruitment.dto.CandidateDto;
import com.hrms.recruitment.entity.Candidate;
import com.hrms.recruitment.repository.ApplicationRepository;
import com.hrms.recruitment.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository   candidateRepository;
    private final ApplicationRepository applicationRepository;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Transactional
    public CandidateDto.Response create(String tenantId, CandidateDto.CreateRequest req,
                                         String currentUser) {
        if (candidateRepository.existsByTenantIdAndEmailAndDeletedFalse(tenantId, req.getEmail())) {
            throw new BusinessException("DUPLICATE_CANDIDATE",
                    "A candidate with email '" + req.getEmail() + "' already exists");
        }
        Candidate c = new Candidate();
        c.setTenantId(tenantId);
        c.setCreatedBy(currentUser);
        applyFields(c, req);
        return toResponse(candidateRepository.save(c));
    }

    @Transactional
    public CandidateDto.Response update(String tenantId, UUID id,
                                         CandidateDto.UpdateRequest req, String currentUser) {
        Candidate c = getEntity(tenantId, id);
        if (req.getFirstName() != null)             c.setFirstName(req.getFirstName());
        if (req.getLastName() != null)              c.setLastName(req.getLastName());
        if (req.getPhone() != null)                 c.setPhone(req.getPhone());
        if (req.getCurrentCompany() != null)        c.setCurrentCompany(req.getCurrentCompany());
        if (req.getCurrentTitle() != null)          c.setCurrentTitle(req.getCurrentTitle());
        if (req.getTotalExperienceYears() != null)  c.setTotalExperienceYears(req.getTotalExperienceYears());
        if (req.getResumeUrl() != null)             c.setResumeUrl(req.getResumeUrl());
        if (req.getLinkedinUrl() != null)           c.setLinkedinUrl(req.getLinkedinUrl());
        if (req.getSource() != null)                c.setSource(req.getSource());
        if (req.getAgencyId() != null)              c.setAgencyId(req.getAgencyId());
        if (req.getReferredBy() != null)            c.setReferredBy(req.getReferredBy());
        if (req.getTags() != null)                  c.setTags(req.getTags());
        c.setUpdatedBy(currentUser);
        return toResponse(candidateRepository.save(c));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        Candidate c = getEntity(tenantId, id);
        c.setDeleted(true);
        c.setUpdatedBy(currentUser);
        candidateRepository.save(c);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public CandidateDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<CandidateDto.Response> list(String tenantId, Pageable pageable) {
        return candidateRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CandidateDto.Response> search(String tenantId, String q, Pageable pageable) {
        return candidateRepository.search(tenantId, q, pageable).map(this::toResponse);
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize())
                .total(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    // ── Package-private helpers ────────────────────────────────────────────────

    Candidate getEntity(String tenantId, UUID id) {
        return candidateRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", id));
    }

    CandidateDto.Summary toSummary(Candidate c) {
        return CandidateDto.Summary.builder()
                .id(c.getId())
                .fullName(c.getFirstName() + " " + c.getLastName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .currentTitle(c.getCurrentTitle())
                .currentCompany(c.getCurrentCompany())
                .totalExperienceYears(c.getTotalExperienceYears())
                .resumeUrl(c.getResumeUrl())
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void applyFields(Candidate c, CandidateDto.CreateRequest req) {
        c.setFirstName(req.getFirstName());
        c.setLastName(req.getLastName());
        c.setEmail(req.getEmail());
        c.setPhone(req.getPhone());
        c.setCurrentCompany(req.getCurrentCompany());
        c.setCurrentTitle(req.getCurrentTitle());
        c.setTotalExperienceYears(req.getTotalExperienceYears());
        c.setResumeUrl(req.getResumeUrl());
        c.setLinkedinUrl(req.getLinkedinUrl());
        c.setSource(req.getSource());
        c.setAgencyId(req.getAgencyId());
        c.setReferredBy(req.getReferredBy());
        c.setTags(req.getTags());
    }

    private CandidateDto.Response toResponse(Candidate c) {
        int appCount = applicationRepository
                .countByTenantIdAndCandidateIdAndDeletedFalse(c.getTenantId(), c.getId());
        return CandidateDto.Response.builder()
                .id(c.getId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .fullName(c.getFirstName() + " " + c.getLastName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .currentCompany(c.getCurrentCompany())
                .currentTitle(c.getCurrentTitle())
                .totalExperienceYears(c.getTotalExperienceYears())
                .resumeUrl(c.getResumeUrl())
                .linkedinUrl(c.getLinkedinUrl())
                .source(c.getSource())
                .agencyId(c.getAgencyId())
                .referredBy(c.getReferredBy())
                .tags(c.getTags())
                .applicationCount(appCount)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
