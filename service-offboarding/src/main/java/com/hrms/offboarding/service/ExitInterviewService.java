package com.hrms.offboarding.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.offboarding.dto.ExitInterviewDto;
import com.hrms.offboarding.entity.ExitInterview;
import com.hrms.offboarding.entity.ExitInterview.InterviewStatus;
import com.hrms.offboarding.repository.ExitInterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExitInterviewService {

    private final ExitInterviewRepository exitInterviewRepository;

    @Transactional
    public ExitInterviewDto.Response create(String tenantId, ExitInterviewDto.CreateRequest req, String currentUser) {
        ExitInterview interview = new ExitInterview();
        interview.setTenantId(tenantId);
        interview.setSeparationId(req.getSeparationId());
        interview.setInterviewerId(req.getInterviewerId());
        interview.setScheduledAt(req.getScheduledAt());
        interview.setStatus(InterviewStatus.SCHEDULED);
        interview.setCreatedBy(currentUser);
        interview.setUpdatedBy(currentUser);
        return toResponse(exitInterviewRepository.save(interview));
    }

    @Transactional(readOnly = true)
    public ExitInterviewDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<ExitInterviewDto.Response> list(String tenantId, Pageable pageable) {
        return exitInterviewRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public ExitInterviewDto.Response update(String tenantId, UUID id, ExitInterviewDto.UpdateRequest req, String currentUser) {
        ExitInterview interview = getEntity(tenantId, id);
        if (req.getInterviewerId() != null) {
            interview.setInterviewerId(req.getInterviewerId());
        }
        if (req.getScheduledAt() != null) {
            interview.setScheduledAt(req.getScheduledAt());
        }
        if (req.getCompletedAt() != null) {
            interview.setCompletedAt(req.getCompletedAt());
        }
        if (req.getSatisfactionRating() != null) {
            interview.setSatisfactionRating(req.getSatisfactionRating());
        }
        if (req.getReasonForLeaving() != null) {
            interview.setReasonForLeaving(req.getReasonForLeaving());
        }
        if (req.getWouldRejoin() != null) {
            interview.setWouldRejoin(req.getWouldRejoin());
        }
        if (req.getFeedback() != null) {
            interview.setFeedback(req.getFeedback());
        }
        if (req.getStatus() != null) {
            interview.setStatus(req.getStatus());
        }
        interview.setUpdatedBy(currentUser);
        return toResponse(exitInterviewRepository.save(interview));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        ExitInterview interview = getEntity(tenantId, id);
        interview.setDeleted(true);
        interview.setUpdatedBy(currentUser);
        exitInterviewRepository.save(interview);
    }

    @Transactional(readOnly = true)
    public List<ExitInterviewDto.Response> listBySeparation(String tenantId, UUID separationId) {
        return exitInterviewRepository
                .findByTenantIdAndSeparationIdAndDeletedFalse(tenantId, separationId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExitInterviewDto.Response complete(String tenantId, UUID id, ExitInterviewDto.UpdateRequest req, String currentUser) {
        ExitInterview interview = getEntity(tenantId, id);
        interview.setCompletedAt(req.getCompletedAt() != null ? req.getCompletedAt() : Instant.now());
        if (req.getSatisfactionRating() != null) {
            interview.setSatisfactionRating(req.getSatisfactionRating());
        }
        if (req.getReasonForLeaving() != null) {
            interview.setReasonForLeaving(req.getReasonForLeaving());
        }
        if (req.getWouldRejoin() != null) {
            interview.setWouldRejoin(req.getWouldRejoin());
        }
        if (req.getFeedback() != null) {
            interview.setFeedback(req.getFeedback());
        }
        interview.setStatus(InterviewStatus.COMPLETED);
        interview.setUpdatedBy(currentUser);
        return toResponse(exitInterviewRepository.save(interview));
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

    private ExitInterview getEntity(String tenantId, UUID id) {
        return exitInterviewRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("ExitInterview", "id", id));
    }

    private ExitInterviewDto.Response toResponse(ExitInterview i) {
        return ExitInterviewDto.Response.builder()
                .id(i.getId())
                .tenantId(i.getTenantId())
                .separationId(i.getSeparationId())
                .interviewerId(i.getInterviewerId())
                .scheduledAt(i.getScheduledAt())
                .completedAt(i.getCompletedAt())
                .satisfactionRating(i.getSatisfactionRating())
                .reasonForLeaving(i.getReasonForLeaving())
                .wouldRejoin(i.getWouldRejoin())
                .feedback(i.getFeedback())
                .status(i.getStatus())
                .createdBy(i.getCreatedBy())
                .updatedBy(i.getUpdatedBy())
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .build();
    }
}
