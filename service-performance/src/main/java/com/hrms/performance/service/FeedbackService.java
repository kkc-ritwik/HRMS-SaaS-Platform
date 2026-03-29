package com.hrms.performance.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.performance.dto.FeedbackDto;
import com.hrms.performance.entity.Feedback;
import com.hrms.performance.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    @Transactional
    public FeedbackDto.Response give(String tenantId, UUID fromEmployeeId,
                                      FeedbackDto.CreateRequest req, String currentUser) {
        Feedback f = new Feedback();
        f.setTenantId(tenantId);
        f.setFromEmployeeId(fromEmployeeId);
        f.setToEmployeeId(req.getToEmployeeId());
        f.setFeedbackType(req.getFeedbackType());
        f.setVisibility(req.getVisibility());
        f.setCycleId(req.getCycleId());
        f.setContext(req.getContext());
        f.setMessage(req.getMessage());
        f.setTags(req.getTags());
        f.setAnonymous(req.isAnonymous());
        f.setCreatedBy(currentUser);
        return toResponse(feedbackRepository.save(f));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        Feedback f = getEntity(tenantId, id);
        f.setDeleted(true);
        f.setUpdatedBy(currentUser);
        feedbackRepository.save(f);
    }

    @Transactional(readOnly = true)
    public FeedbackDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<FeedbackDto.Response> received(String tenantId, UUID employeeId,
                                                boolean isManager, Pageable pageable) {
        return feedbackRepository.findReceived(tenantId, employeeId, isManager, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackDto.Response> given(String tenantId, UUID fromEmployeeId,
                                             Pageable pageable) {
        return feedbackRepository
                .findByTenantIdAndFromEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(
                        tenantId, fromEmployeeId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackDto.Response> publicWall(String tenantId, Pageable pageable) {
        return feedbackRepository.findPublicAppreciations(tenantId, pageable)
                .map(this::toResponse);
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize())
                .total(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    private Feedback getEntity(String tenantId, UUID id) {
        return feedbackRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", id));
    }

    private FeedbackDto.Response toResponse(Feedback f) {
        // Mask sender name if anonymous (caller resolves name from core-hr)
        UUID fromId = f.isAnonymous() ? null : f.getFromEmployeeId();
        return FeedbackDto.Response.builder()
                .id(f.getId())
                .fromEmployeeId(fromId)
                .fromEmployeeName(f.isAnonymous() ? "Anonymous" : null)
                .toEmployeeId(f.getToEmployeeId())
                .feedbackType(f.getFeedbackType())
                .visibility(f.getVisibility())
                .cycleId(f.getCycleId())
                .context(f.getContext())
                .message(f.getMessage())
                .tags(f.getTags())
                .anonymous(f.isAnonymous())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
