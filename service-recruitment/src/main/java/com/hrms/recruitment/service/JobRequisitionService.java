package com.hrms.recruitment.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.recruitment.dto.JobRequisitionDto;
import com.hrms.recruitment.entity.JobRequisition;
import com.hrms.recruitment.repository.JobRequisitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobRequisitionService {

    private final JobRequisitionRepository requisitionRepository;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Transactional
    public JobRequisitionDto.Response create(String tenantId, JobRequisitionDto.CreateRequest req,
                                              String currentUser) {
        JobRequisition r = new JobRequisition();
        r.setTenantId(tenantId);
        r.setRequestedBy(currentUser);
        r.setCreatedBy(currentUser);
        r.setStatus(JobRequisition.RequisitionStatus.DRAFT);
        applyFields(r, req);
        return toResponse(requisitionRepository.save(r));
    }

    @Transactional
    public JobRequisitionDto.Response update(String tenantId, UUID id,
                                              JobRequisitionDto.UpdateRequest req,
                                              String currentUser) {
        JobRequisition r = getEntity(tenantId, id);
        if (r.getStatus() != JobRequisition.RequisitionStatus.DRAFT &&
            r.getStatus() != JobRequisition.RequisitionStatus.PENDING_APPROVAL &&
            r.getStatus() != JobRequisition.RequisitionStatus.ON_HOLD) {
            throw new BusinessException("INVALID_STATUS",
                    "Only DRAFT, PENDING_APPROVAL or ON_HOLD requisitions can be updated");
        }
        if (req.getTitle() != null)            r.setTitle(req.getTitle());
        if (req.getDepartmentId() != null)     r.setDepartmentId(req.getDepartmentId());
        if (req.getLocation() != null)         r.setLocation(req.getLocation());
        if (req.getEmploymentType() != null)   r.setEmploymentType(req.getEmploymentType());
        if (req.getPositionsCount() != null)   r.setPositionsCount(req.getPositionsCount());
        if (req.getDescription() != null)      r.setDescription(req.getDescription());
        if (req.getRequirements() != null)     r.setRequirements(req.getRequirements());
        if (req.getSalaryMin() != null)        r.setSalaryMin(req.getSalaryMin());
        if (req.getSalaryMax() != null)        r.setSalaryMax(req.getSalaryMax());
        if (req.getPriority() != null)         r.setPriority(req.getPriority());
        if (req.getTargetDate() != null)       r.setTargetDate(req.getTargetDate());
        if (req.getSource() != null)           r.setSource(req.getSource());
        if (req.getAgencyId() != null)         r.setAgencyId(req.getAgencyId());
        r.setUpdatedBy(currentUser);
        return toResponse(requisitionRepository.save(r));
    }

    // ── Workflow transitions ───────────────────────────────────────────────────

    @Transactional
    public JobRequisitionDto.Response submit(String tenantId, UUID id, String currentUser) {
        return transition(tenantId, id, currentUser,
                JobRequisition.RequisitionStatus.DRAFT,
                JobRequisition.RequisitionStatus.PENDING_APPROVAL, false);
    }

    @Transactional
    public JobRequisitionDto.Response approve(String tenantId, UUID id, String currentUser) {
        JobRequisition r = getEntity(tenantId, id);
        if (r.getStatus() != JobRequisition.RequisitionStatus.PENDING_APPROVAL) {
            throw new BusinessException("INVALID_STATUS",
                    "Only PENDING_APPROVAL requisitions can be approved. Current: " + r.getStatus());
        }
        r.setStatus(JobRequisition.RequisitionStatus.APPROVED);
        r.setApprovedBy(currentUser);
        r.setApprovedAt(Instant.now());
        r.setUpdatedBy(currentUser);
        return toResponse(requisitionRepository.save(r));
    }

    @Transactional
    public JobRequisitionDto.Response activate(String tenantId, UUID id, String currentUser) {
        return transition(tenantId, id, currentUser,
                JobRequisition.RequisitionStatus.APPROVED,
                JobRequisition.RequisitionStatus.ACTIVE, false);
    }

    @Transactional
    public JobRequisitionDto.Response hold(String tenantId, UUID id, String currentUser) {
        return transition(tenantId, id, currentUser,
                JobRequisition.RequisitionStatus.ACTIVE,
                JobRequisition.RequisitionStatus.ON_HOLD, false);
    }

    @Transactional
    public JobRequisitionDto.Response close(String tenantId, UUID id, String currentUser) {
        JobRequisition r = getEntity(tenantId, id);
        if (r.getStatus() == JobRequisition.RequisitionStatus.CANCELLED ||
            r.getStatus() == JobRequisition.RequisitionStatus.CLOSED) {
            throw new BusinessException("INVALID_STATUS", "Requisition is already " + r.getStatus());
        }
        r.setStatus(JobRequisition.RequisitionStatus.CLOSED);
        r.setUpdatedBy(currentUser);
        return toResponse(requisitionRepository.save(r));
    }

    @Transactional
    public JobRequisitionDto.Response cancel(String tenantId, UUID id, String currentUser) {
        JobRequisition r = getEntity(tenantId, id);
        if (r.getStatus() == JobRequisition.RequisitionStatus.CLOSED ||
            r.getStatus() == JobRequisition.RequisitionStatus.CANCELLED) {
            throw new BusinessException("INVALID_STATUS", "Requisition is already " + r.getStatus());
        }
        r.setStatus(JobRequisition.RequisitionStatus.CANCELLED);
        r.setUpdatedBy(currentUser);
        return toResponse(requisitionRepository.save(r));
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public JobRequisitionDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<JobRequisitionDto.Response> list(String tenantId, Pageable pageable) {
        return requisitionRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<JobRequisitionDto.Response> listByStatus(String tenantId,
                                                           JobRequisition.RequisitionStatus status,
                                                           Pageable pageable) {
        return requisitionRepository
                .findByTenantIdAndStatusAndDeletedFalseOrderByCreatedAtDesc(tenantId, status, pageable)
                .map(this::toResponse);
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize())
                .total(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    /** Called from ApplicationService when a candidate is hired. */
    @Transactional
    public void incrementFilled(String tenantId, UUID requisitionId) {
        JobRequisition r = getEntity(tenantId, requisitionId);
        r.setPositionsFilled(r.getPositionsFilled() + 1);
        if (r.getPositionsFilled() >= r.getPositionsCount()) {
            r.setStatus(JobRequisition.RequisitionStatus.CLOSED);
        }
        requisitionRepository.save(r);
    }

    JobRequisition getEntity(String tenantId, UUID id) {
        return requisitionRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("JobRequisition", "id", id));
    }

    private void applyFields(JobRequisition r, JobRequisitionDto.CreateRequest req) {
        r.setTitle(req.getTitle());
        r.setDepartmentId(req.getDepartmentId());
        r.setLocation(req.getLocation());
        r.setEmploymentType(req.getEmploymentType());
        r.setPositionsCount(req.getPositionsCount());
        r.setDescription(req.getDescription());
        r.setRequirements(req.getRequirements());
        r.setSalaryMin(req.getSalaryMin());
        r.setSalaryMax(req.getSalaryMax());
        r.setPriority(req.getPriority());
        r.setTargetDate(req.getTargetDate());
        r.setSource(req.getSource());
        r.setAgencyId(req.getAgencyId());
    }

    private JobRequisitionDto.Response toResponse(JobRequisition r) {
        return JobRequisitionDto.Response.builder()
                .id(r.getId())
                .title(r.getTitle())
                .departmentId(r.getDepartmentId())
                .location(r.getLocation())
                .employmentType(r.getEmploymentType())
                .positionsCount(r.getPositionsCount())
                .positionsFilled(r.getPositionsFilled())
                .openPositions(Math.max(0, r.getPositionsCount() - r.getPositionsFilled()))
                .description(r.getDescription())
                .requirements(r.getRequirements())
                .salaryMin(r.getSalaryMin())
                .salaryMax(r.getSalaryMax())
                .status(r.getStatus())
                .priority(r.getPriority())
                .targetDate(r.getTargetDate())
                .source(r.getSource())
                .agencyId(r.getAgencyId())
                .requestedBy(r.getRequestedBy())
                .approvedBy(r.getApprovedBy())
                .approvedAt(r.getApprovedAt())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    private JobRequisitionDto.Response transition(String tenantId, UUID id, String currentUser,
                                                   JobRequisition.RequisitionStatus required,
                                                   JobRequisition.RequisitionStatus next,
                                                   boolean setApproved) {
        JobRequisition r = getEntity(tenantId, id);
        if (r.getStatus() != required) {
            throw new BusinessException("INVALID_STATUS",
                    "Expected status " + required + " but got " + r.getStatus());
        }
        r.setStatus(next);
        r.setUpdatedBy(currentUser);
        return toResponse(requisitionRepository.save(r));
    }
}
