package com.hrms.performance.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.performance.dto.PipPlanDto;
import com.hrms.performance.entity.PipPlan;
import com.hrms.performance.repository.PipPlanRepository;
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
public class PipPlanService {

    private final PipPlanRepository pipPlanRepository;

    @Transactional
    public PipPlanDto.Response create(String tenantId, PipPlanDto.CreateRequest req,
                                       String currentUser) {
        PipPlan p = new PipPlan();
        p.setTenantId(tenantId);
        p.setEmployeeId(req.getEmployeeId());
        p.setManagerId(req.getManagerId());
        p.setHrManagerId(req.getHrManagerId());
        p.setReviewCycleId(req.getReviewCycleId());
        p.setTitle(req.getTitle());
        p.setReason(req.getReason());
        p.setStartDate(req.getStartDate());
        p.setEndDate(req.getEndDate());
        p.setStatus(PipPlan.PipStatus.DRAFT);
        p.setImprovementAreas(req.getImprovementAreas());
        p.setSupportProvided(req.getSupportProvided());
        p.setCheckInFrequency(req.getCheckInFrequency());
        p.setCreatedBy(currentUser);
        return toResponse(pipPlanRepository.save(p));
    }

    @Transactional
    public PipPlanDto.Response update(String tenantId, UUID id,
                                       PipPlanDto.UpdateRequest req, String currentUser) {
        PipPlan p = getEntity(tenantId, id);
        if (p.getStatus() == PipPlan.PipStatus.COMPLETED ||
            p.getStatus() == PipPlan.PipStatus.FAILED ||
            p.getStatus() == PipPlan.PipStatus.WITHDRAWN) {
            throw new BusinessException("PIP_TERMINAL", "PIP plan is in terminal status: " + p.getStatus());
        }
        if (req.getTitle() != null)              p.setTitle(req.getTitle());
        if (req.getReason() != null)             p.setReason(req.getReason());
        if (req.getEndDate() != null)            p.setEndDate(req.getEndDate());
        if (req.getImprovementAreas() != null)   p.setImprovementAreas(req.getImprovementAreas());
        if (req.getSupportProvided() != null)    p.setSupportProvided(req.getSupportProvided());
        if (req.getCheckInFrequency() != null)   p.setCheckInFrequency(req.getCheckInFrequency());
        if (req.getOutcomeNotes() != null)       p.setOutcomeNotes(req.getOutcomeNotes());

        if (req.getStatus() != null) {
            p.setStatus(req.getStatus());
            if (req.getStatus() == PipPlan.PipStatus.COMPLETED ||
                req.getStatus() == PipPlan.PipStatus.FAILED ||
                req.getStatus() == PipPlan.PipStatus.WITHDRAWN) {
                p.setClosedAt(Instant.now());
            }
        }
        p.setUpdatedBy(currentUser);
        return toResponse(pipPlanRepository.save(p));
    }

    @Transactional
    public PipPlanDto.Response activate(String tenantId, UUID id, String currentUser) {
        PipPlan p = getEntity(tenantId, id);
        if (p.getStatus() != PipPlan.PipStatus.DRAFT) {
            throw new BusinessException("INVALID_STATUS", "Only DRAFT PIP plans can be activated");
        }
        p.setStatus(PipPlan.PipStatus.ACTIVE);
        p.setUpdatedBy(currentUser);
        return toResponse(pipPlanRepository.save(p));
    }

    @Transactional(readOnly = true)
    public PipPlanDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<PipPlanDto.Response> list(String tenantId, Pageable pageable) {
        return pipPlanRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<PipPlanDto.Response> listForEmployee(String tenantId, UUID employeeId) {
        return pipPlanRepository.findByTenantIdAndEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(
                        tenantId, employeeId)
                .stream().map(this::toResponse).toList();
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize())
                .total(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    private PipPlan getEntity(String tenantId, UUID id) {
        return pipPlanRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("PipPlan", "id", id));
    }

    private PipPlanDto.Response toResponse(PipPlan p) {
        return PipPlanDto.Response.builder()
                .id(p.getId()).employeeId(p.getEmployeeId()).managerId(p.getManagerId())
                .hrManagerId(p.getHrManagerId()).reviewCycleId(p.getReviewCycleId())
                .title(p.getTitle()).reason(p.getReason())
                .startDate(p.getStartDate()).endDate(p.getEndDate())
                .status(p.getStatus()).improvementAreas(p.getImprovementAreas())
                .supportProvided(p.getSupportProvided()).checkInFrequency(p.getCheckInFrequency())
                .outcomeNotes(p.getOutcomeNotes()).closedAt(p.getClosedAt())
                .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
                .build();
    }
}
