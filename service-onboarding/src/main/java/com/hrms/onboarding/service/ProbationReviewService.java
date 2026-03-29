package com.hrms.onboarding.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.onboarding.dto.ProbationReviewDto;
import com.hrms.onboarding.entity.ProbationReview;
import com.hrms.onboarding.entity.ProbationReview.ReviewStatus;
import com.hrms.onboarding.repository.ProbationReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProbationReviewService {

    private final ProbationReviewRepository probationReviewRepository;

    @Transactional
    public ProbationReviewDto.Response create(String tenantId, ProbationReviewDto.CreateRequest req, String currentUser) {
        ProbationReview review = new ProbationReview();
        review.setTenantId(tenantId);
        review.setEmployeeId(req.getEmployeeId());
        review.setReviewerId(req.getReviewerId());
        review.setReviewDate(req.getReviewDate());
        review.setPeriodMonths(req.getPeriodMonths());
        review.setStatus(ReviewStatus.SCHEDULED);
        review.setComments(req.getComments());
        review.setCreatedBy(currentUser);
        review.setUpdatedBy(currentUser);
        return toResponse(probationReviewRepository.save(review));
    }

    @Transactional
    public ProbationReviewDto.Response update(String tenantId, UUID id, ProbationReviewDto.UpdateRequest req, String currentUser) {
        ProbationReview review = getEntity(tenantId, id);
        if (req.getEmployeeId() != null) {
            review.setEmployeeId(req.getEmployeeId());
        }
        if (req.getReviewerId() != null) {
            review.setReviewerId(req.getReviewerId());
        }
        if (req.getReviewDate() != null) {
            review.setReviewDate(req.getReviewDate());
        }
        if (req.getPeriodMonths() != null) {
            review.setPeriodMonths(req.getPeriodMonths());
        }
        if (req.getStatus() != null) {
            review.setStatus(req.getStatus());
        }
        if (req.getOverallRating() != null) {
            review.setOverallRating(req.getOverallRating());
        }
        if (req.getComments() != null) {
            review.setComments(req.getComments());
        }
        if (req.getExtendedUntil() != null) {
            review.setExtendedUntil(req.getExtendedUntil());
        }
        review.setUpdatedBy(currentUser);
        return toResponse(probationReviewRepository.save(review));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        ProbationReview review = getEntity(tenantId, id);
        review.setDeleted(true);
        review.setUpdatedBy(currentUser);
        probationReviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public ProbationReviewDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<ProbationReviewDto.Response> list(String tenantId, Pageable pageable) {
        return probationReviewRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ProbationReviewDto.Response> listByEmployee(String tenantId, UUID employeeId) {
        return probationReviewRepository
                .findByTenantIdAndEmployeeIdAndDeletedFalse(tenantId, employeeId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    private ProbationReview getEntity(String tenantId, UUID id) {
        return probationReviewRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProbationReview", "id", id));
    }

    private ProbationReviewDto.Response toResponse(ProbationReview review) {
        return ProbationReviewDto.Response.builder()
                .id(review.getId())
                .tenantId(review.getTenantId())
                .employeeId(review.getEmployeeId())
                .reviewerId(review.getReviewerId())
                .reviewDate(review.getReviewDate())
                .periodMonths(review.getPeriodMonths())
                .status(review.getStatus())
                .overallRating(review.getOverallRating())
                .comments(review.getComments())
                .extendedUntil(review.getExtendedUntil())
                .createdBy(review.getCreatedBy())
                .updatedBy(review.getUpdatedBy())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
