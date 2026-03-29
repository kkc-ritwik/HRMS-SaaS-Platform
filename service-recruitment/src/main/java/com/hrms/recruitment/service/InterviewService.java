package com.hrms.recruitment.service;

import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.DuplicateResourceException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.recruitment.dto.InterviewDto;
import com.hrms.recruitment.entity.Interview;
import com.hrms.recruitment.entity.InterviewPanelist;
import com.hrms.recruitment.repository.InterviewPanelistRepository;
import com.hrms.recruitment.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository         interviewRepository;
    private final InterviewPanelistRepository panelistRepository;

    // ── Schedule ──────────────────────────────────────────────────────────────

    @Transactional
    public InterviewDto.Response schedule(String tenantId, InterviewDto.ScheduleRequest req,
                                           String currentUser) {
        Interview iv = new Interview();
        iv.setTenantId(tenantId);
        iv.setApplicationId(req.getApplicationId());
        iv.setInterviewType(req.getInterviewType());
        iv.setRoundNumber(req.getRoundNumber());
        iv.setScheduledAt(req.getScheduledAt());
        iv.setDurationMinutes(req.getDurationMinutes());
        iv.setMode(req.getMode());
        iv.setMeetingLink(req.getMeetingLink());
        iv.setVenue(req.getVenue());
        iv.setStatus(Interview.InterviewStatus.SCHEDULED);
        iv.setCreatedBy(currentUser);

        Interview saved = interviewRepository.save(iv);

        // Add panelists
        if (req.getPanelists() != null) {
            for (InterviewDto.PanelistRequest pr : req.getPanelists()) {
                addPanelistInternal(tenantId, saved.getId(), pr.getInterviewerId(), pr.getRole(), currentUser);
            }
        }

        return toResponse(saved);
    }

    // ── Status update ────────────────────────────────────────────────────────

    @Transactional
    public InterviewDto.Response updateStatus(String tenantId, UUID id,
                                               Interview.InterviewStatus status,
                                               String currentUser) {
        Interview iv = getEntity(tenantId, id);
        if (iv.getStatus() == Interview.InterviewStatus.COMPLETED ||
            iv.getStatus() == Interview.InterviewStatus.CANCELLED) {
            throw new BusinessException("INVALID_STATUS",
                    "Interview is already in terminal status: " + iv.getStatus());
        }
        iv.setStatus(status);
        iv.setUpdatedBy(currentUser);
        return toResponse(interviewRepository.save(iv));
    }

    // ── Feedback ─────────────────────────────────────────────────────────────

    @Transactional
    public InterviewDto.Response submitFeedback(String tenantId, UUID id,
                                                 InterviewDto.FeedbackRequest req,
                                                 String currentUser) {
        Interview iv = getEntity(tenantId, id);
        iv.setOverallRating(req.getOverallRating());
        iv.setRecommendation(req.getRecommendation());
        iv.setFeedback(req.getFeedback());
        iv.setStatus(Interview.InterviewStatus.COMPLETED);
        iv.setUpdatedBy(currentUser);
        return toResponse(interviewRepository.save(iv));
    }

    @Transactional
    public InterviewDto.PanelistSummary submitPanelistFeedback(String tenantId, UUID interviewId,
                                                                UUID panelistId,
                                                                InterviewDto.PanelistFeedbackRequest req,
                                                                String currentUser) {
        getEntity(tenantId, interviewId); // verify tenant access
        InterviewPanelist panelist = panelistRepository.findByIdAndTenantIdAndDeletedFalse(panelistId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("InterviewPanelist", "id", panelistId));
        if (!panelist.getInterviewId().equals(interviewId)) {
            throw new BusinessException("PANELIST_MISMATCH", "Panelist does not belong to this interview");
        }
        panelist.setRating(req.getRating());
        panelist.setFeedback(req.getFeedback());
        panelist.setSubmittedAt(Instant.now());
        panelist.setUpdatedBy(currentUser);
        return toPanelistSummary(panelistRepository.save(panelist));
    }

    // ── Panelist management ───────────────────────────────────────────────────

    @Transactional
    public InterviewDto.PanelistSummary addPanelist(String tenantId, UUID interviewId,
                                                     InterviewDto.PanelistRequest req,
                                                     String currentUser) {
        getEntity(tenantId, interviewId); // verify tenant access
        if (panelistRepository.existsByInterviewIdAndInterviewerIdAndDeletedFalse(
                interviewId, req.getInterviewerId())) {
            throw new DuplicateResourceException("InterviewPanelist", "interviewerId",
                    req.getInterviewerId());
        }
        return toPanelistSummary(
                addPanelistInternal(tenantId, interviewId, req.getInterviewerId(), req.getRole(), currentUser));
    }

    @Transactional
    public void removePanelist(String tenantId, UUID interviewId, UUID panelistId, String currentUser) {
        getEntity(tenantId, interviewId); // verify tenant access
        InterviewPanelist panelist = panelistRepository.findByIdAndTenantIdAndDeletedFalse(panelistId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("InterviewPanelist", "id", panelistId));
        panelist.setDeleted(true);
        panelist.setUpdatedBy(currentUser);
        panelistRepository.save(panelist);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public InterviewDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<InterviewDto.Response> listByApplication(String tenantId, UUID applicationId) {
        return interviewRepository
                .findByApplicationIdAndTenantIdAndDeletedFalseOrderByRoundNumberAsc(
                        applicationId, tenantId)
                .stream().map(this::toResponse).toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private InterviewPanelist addPanelistInternal(String tenantId, UUID interviewId,
                                                   UUID interviewerId,
                                                   InterviewPanelist.PanelistRole role,
                                                   String currentUser) {
        InterviewPanelist p = new InterviewPanelist();
        p.setTenantId(tenantId);
        p.setInterviewId(interviewId);
        p.setInterviewerId(interviewerId);
        p.setRole(role != null ? role : InterviewPanelist.PanelistRole.PANELIST);
        p.setCreatedBy(currentUser);
        return panelistRepository.save(p);
    }

    private Interview getEntity(String tenantId, UUID id) {
        return interviewRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", id));
    }

    private InterviewDto.Response toResponse(Interview iv) {
        List<InterviewDto.PanelistSummary> panelists = panelistRepository
                .findByInterviewIdAndDeletedFalse(iv.getId())
                .stream().map(this::toPanelistSummary).toList();

        return InterviewDto.Response.builder()
                .id(iv.getId())
                .applicationId(iv.getApplicationId())
                .interviewType(iv.getInterviewType())
                .roundNumber(iv.getRoundNumber())
                .scheduledAt(iv.getScheduledAt())
                .durationMinutes(iv.getDurationMinutes())
                .mode(iv.getMode())
                .meetingLink(iv.getMeetingLink())
                .venue(iv.getVenue())
                .status(iv.getStatus())
                .overallRating(iv.getOverallRating())
                .recommendation(iv.getRecommendation())
                .feedback(iv.getFeedback())
                .panelists(panelists)
                .createdAt(iv.getCreatedAt())
                .updatedAt(iv.getUpdatedAt())
                .build();
    }

    private InterviewDto.PanelistSummary toPanelistSummary(InterviewPanelist p) {
        return InterviewDto.PanelistSummary.builder()
                .id(p.getId())
                .interviewerId(p.getInterviewerId())
                .role(p.getRole())
                .rating(p.getRating())
                .feedback(p.getFeedback())
                .submittedAt(p.getSubmittedAt())
                .build();
    }
}
