package com.hrms.recruitment.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.recruitment.dto.ApplicationDto;
import com.hrms.recruitment.dto.CandidateDto;
import com.hrms.recruitment.entity.Application;
import com.hrms.recruitment.entity.Candidate;
import com.hrms.recruitment.entity.JobRequisition;
import com.hrms.recruitment.repository.ApplicationRepository;
import com.hrms.recruitment.repository.CandidateRepository;
import com.hrms.recruitment.repository.JobRequisitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository    applicationRepository;
    private final CandidateRepository      candidateRepository;
    private final JobRequisitionRepository requisitionRepository;
    private final JobRequisitionService    jobRequisitionService;

    // ── Apply ─────────────────────────────────────────────────────────────────

    @Transactional
    public ApplicationDto.Response apply(String tenantId, ApplicationDto.CreateRequest req,
                                          String currentUser) {
        // Validate requisition exists and is ACTIVE
        JobRequisition req_ = requisitionRepository.findByIdAndTenantIdAndDeletedFalse(
                        req.getRequisitionId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("JobRequisition", "id", req.getRequisitionId()));
        if (req_.getStatus() != JobRequisition.RequisitionStatus.ACTIVE) {
            throw new BusinessException("REQUISITION_NOT_ACTIVE",
                    "Only ACTIVE job requisitions accept applications. Current: " + req_.getStatus());
        }
        // Validate candidate exists
        if (!candidateRepository.existsByTenantIdAndEmailAndDeletedFalse(
                tenantId, candidateRepository.findByIdAndTenantIdAndDeletedFalse(
                        req.getCandidateId(), tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", req.getCandidateId()))
                        .getEmail())) {
            throw new ResourceNotFoundException("Candidate", "id", req.getCandidateId());
        }
        // Duplicate check
        if (applicationRepository.existsByTenantIdAndRequisitionIdAndCandidateIdAndDeletedFalse(
                tenantId, req.getRequisitionId(), req.getCandidateId())) {
            throw new BusinessException("DUPLICATE_APPLICATION",
                    "This candidate has already applied to this requisition");
        }

        Application app = new Application();
        app.setTenantId(tenantId);
        app.setRequisitionId(req.getRequisitionId());
        app.setCandidateId(req.getCandidateId());
        app.setStage(Application.ApplicationStage.APPLIED);
        app.setAppliedAt(Instant.now());
        app.setStageChangedAt(Instant.now());
        app.setCurrentCtc(req.getCurrentCtc());
        app.setExpectedCtc(req.getExpectedCtc());
        app.setNoticePeriodDays(req.getNoticePeriodDays());
        app.setNotes(req.getNotes());
        app.setCreatedBy(currentUser);

        return toResponse(applicationRepository.save(app));
    }

    // ── Stage transition ─────────────────────────────────────────────────────

    @Transactional
    public ApplicationDto.Response moveStage(String tenantId, UUID id,
                                              ApplicationDto.MoveStageRequest req,
                                              String currentUser) {
        Application app = getEntity(tenantId, id);

        if (app.getStage() == Application.ApplicationStage.HIRED ||
            app.getStage() == Application.ApplicationStage.WITHDRAWN) {
            throw new BusinessException("APPLICATION_TERMINAL",
                    "Application is in terminal stage: " + app.getStage());
        }
        if (req.getStage() == Application.ApplicationStage.REJECTED && req.getRejectionReason() == null) {
            // Allow rejection without reason — just warn via log; don't enforce
        }

        app.setStage(req.getStage());
        app.setStageChangedAt(Instant.now());
        if (req.getRejectionReason() != null) app.setRejectionReason(req.getRejectionReason());
        if (req.getNotes() != null)           app.setNotes(req.getNotes());
        app.setUpdatedBy(currentUser);

        Application saved = applicationRepository.save(app);

        // When hired, increment the requisition's filled count
        if (req.getStage() == Application.ApplicationStage.HIRED) {
            jobRequisitionService.incrementFilled(tenantId, app.getRequisitionId());
        }

        return toResponse(saved);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ApplicationDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<ApplicationDto.Response> listByRequisition(String tenantId, UUID requisitionId,
                                                             Pageable pageable) {
        return applicationRepository
                .findByTenantIdAndRequisitionIdAndDeletedFalseOrderByAppliedAtDesc(
                        tenantId, requisitionId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationDto.Response> listByCandidate(String tenantId, UUID candidateId,
                                                           Pageable pageable) {
        return applicationRepository
                .findByTenantIdAndCandidateIdAndDeletedFalseOrderByAppliedAtDesc(
                        tenantId, candidateId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationDto.Response> listByStage(String tenantId,
                                                       Application.ApplicationStage stage,
                                                       Pageable pageable) {
        return applicationRepository
                .findByTenantIdAndStageAndDeletedFalseOrderByStageChangedAtAsc(
                        tenantId, stage, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ApplicationDto.StageCount> getPipelineCounts(String tenantId, UUID requisitionId) {
        return applicationRepository.stageBreakdown(tenantId, requisitionId)
                .stream()
                .map(row -> ApplicationDto.StageCount.builder()
                        .stage((Application.ApplicationStage) row[0])
                        .count((Long) row[1])
                        .build())
                .toList();
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize())
                .total(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    Application getEntity(String tenantId, UUID id) {
        return applicationRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", id));
    }

    private ApplicationDto.Response toResponse(Application app) {
        CandidateDto.Summary candidateSummary = candidateRepository
                .findByIdAndTenantIdAndDeletedFalse(app.getCandidateId(), app.getTenantId())
                .map(this::toSummary)
                .orElse(null);

        String requisitionTitle = requisitionRepository
                .findByIdAndTenantIdAndDeletedFalse(app.getRequisitionId(), app.getTenantId())
                .map(JobRequisition::getTitle)
                .orElse(null);

        return ApplicationDto.Response.builder()
                .id(app.getId())
                .requisitionId(app.getRequisitionId())
                .requisitionTitle(requisitionTitle)
                .candidateId(app.getCandidateId())
                .candidate(candidateSummary)
                .stage(app.getStage())
                .rejectionReason(app.getRejectionReason())
                .appliedAt(app.getAppliedAt())
                .stageChangedAt(app.getStageChangedAt())
                .currentCtc(app.getCurrentCtc())
                .expectedCtc(app.getExpectedCtc())
                .noticePeriodDays(app.getNoticePeriodDays())
                .notes(app.getNotes())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }

    private CandidateDto.Summary toSummary(Candidate c) {
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
}
