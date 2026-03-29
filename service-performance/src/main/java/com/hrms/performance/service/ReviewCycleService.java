package com.hrms.performance.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.performance.dto.ReviewCycleDto;
import com.hrms.performance.entity.ReviewCycle;
import com.hrms.performance.repository.ReviewCycleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewCycleService {

    private final ReviewCycleRepository cycleRepository;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Transactional
    public ReviewCycleDto.Response create(String tenantId, ReviewCycleDto.CreateRequest req,
                                           String currentUser) {
        ReviewCycle c = new ReviewCycle();
        c.setTenantId(tenantId);
        c.setCreatedBy(currentUser);
        c.setStatus(ReviewCycle.CycleStatus.DRAFT);
        applyFields(c, req);
        return toResponse(cycleRepository.save(c));
    }

    @Transactional
    public ReviewCycleDto.Response update(String tenantId, UUID id,
                                           ReviewCycleDto.UpdateRequest req, String currentUser) {
        ReviewCycle c = getEntity(tenantId, id);
        if (c.getStatus() != ReviewCycle.CycleStatus.DRAFT) {
            throw new BusinessException("CYCLE_NOT_EDITABLE",
                    "Only DRAFT cycles can be updated. Current: " + c.getStatus());
        }
        if (req.getName() != null)                 c.setName(req.getName());
        if (req.getDescription() != null)          c.setDescription(req.getDescription());
        if (req.getPeriodStart() != null)          c.setPeriodStart(req.getPeriodStart());
        if (req.getPeriodEnd() != null)            c.setPeriodEnd(req.getPeriodEnd());
        if (req.getSelfReviewDeadline() != null)   c.setSelfReviewDeadline(req.getSelfReviewDeadline());
        if (req.getManagerReviewDeadline() != null) c.setManagerReviewDeadline(req.getManagerReviewDeadline());
        if (req.getCalibrationDate() != null)      c.setCalibrationDate(req.getCalibrationDate());
        if (req.getIncludeGoalRating() != null)    c.setIncludeGoalRating(req.getIncludeGoalRating());
        if (req.getIncludeCompetencyRating() != null) c.setIncludeCompetencyRating(req.getIncludeCompetencyRating());
        if (req.getInclude360Feedback() != null)   c.setInclude360Feedback(req.getInclude360Feedback());
        if (req.getRatingScale() != null)          c.setRatingScale(req.getRatingScale());
        c.setUpdatedBy(currentUser);
        return toResponse(cycleRepository.save(c));
    }

    // ── Workflow ──────────────────────────────────────────────────────────────

    @Transactional
    public ReviewCycleDto.Response transition(String tenantId, UUID id,
                                               ReviewCycle.CycleStatus next, String currentUser) {
        ReviewCycle c = getEntity(tenantId, id);
        validateTransition(c.getStatus(), next);
        c.setStatus(next);
        c.setUpdatedBy(currentUser);
        return toResponse(cycleRepository.save(c));
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ReviewCycleDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<ReviewCycleDto.Response> list(String tenantId, Pageable pageable) {
        return cycleRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ReviewCycleDto.Response> listByStatus(String tenantId, ReviewCycle.CycleStatus status) {
        return cycleRepository.findByTenantIdAndStatusAndDeletedFalse(tenantId, status)
                .stream().map(this::toResponse).toList();
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize())
                .total(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    // ── Package-private ───────────────────────────────────────────────────────

    ReviewCycle getEntity(String tenantId, UUID id) {
        return cycleRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("ReviewCycle", "id", id));
    }

    ReviewCycleDto.Response toResponse(ReviewCycle c) {
        return ReviewCycleDto.Response.builder()
                .id(c.getId()).name(c.getName()).description(c.getDescription())
                .cycleType(c.getCycleType()).fiscalYear(c.getFiscalYear())
                .periodStart(c.getPeriodStart()).periodEnd(c.getPeriodEnd())
                .status(c.getStatus())
                .selfReviewDeadline(c.getSelfReviewDeadline())
                .managerReviewDeadline(c.getManagerReviewDeadline())
                .calibrationDate(c.getCalibrationDate())
                .includeGoalRating(c.isIncludeGoalRating())
                .includeCompetencyRating(c.isIncludeCompetencyRating())
                .include360Feedback(c.isInclude360Feedback())
                .ratingScale(c.getRatingScale())
                .createdAt(c.getCreatedAt()).updatedAt(c.getUpdatedAt())
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void applyFields(ReviewCycle c, ReviewCycleDto.CreateRequest req) {
        c.setName(req.getName());
        c.setDescription(req.getDescription());
        c.setCycleType(req.getCycleType());
        c.setFiscalYear(req.getFiscalYear());
        c.setPeriodStart(req.getPeriodStart());
        c.setPeriodEnd(req.getPeriodEnd());
        c.setSelfReviewDeadline(req.getSelfReviewDeadline());
        c.setManagerReviewDeadline(req.getManagerReviewDeadline());
        c.setCalibrationDate(req.getCalibrationDate());
        c.setIncludeGoalRating(req.isIncludeGoalRating());
        c.setIncludeCompetencyRating(req.isIncludeCompetencyRating());
        c.setInclude360Feedback(req.isInclude360Feedback());
        c.setRatingScale(req.getRatingScale());
    }

    private void validateTransition(ReviewCycle.CycleStatus current, ReviewCycle.CycleStatus next) {
        boolean valid = switch (next) {
            case ACTIVE         -> current == ReviewCycle.CycleStatus.DRAFT;
            case SELF_REVIEW    -> current == ReviewCycle.CycleStatus.ACTIVE;
            case MANAGER_REVIEW -> current == ReviewCycle.CycleStatus.SELF_REVIEW;
            case CALIBRATION    -> current == ReviewCycle.CycleStatus.MANAGER_REVIEW;
            case FINALIZED      -> current == ReviewCycle.CycleStatus.CALIBRATION;
            case CLOSED         -> current == ReviewCycle.CycleStatus.FINALIZED;
            default -> false;
        };
        if (!valid) {
            throw new BusinessException("INVALID_CYCLE_TRANSITION",
                    "Cannot move cycle from " + current + " to " + next);
        }
    }
}
