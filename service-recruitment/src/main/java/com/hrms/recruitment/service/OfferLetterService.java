package com.hrms.recruitment.service;

import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.recruitment.dto.ApplicationDto;
import com.hrms.recruitment.dto.OfferLetterDto;
import com.hrms.recruitment.entity.Application;
import com.hrms.recruitment.entity.Candidate;
import com.hrms.recruitment.entity.JobRequisition;
import com.hrms.recruitment.entity.OfferLetter;
import com.hrms.recruitment.repository.CandidateRepository;
import com.hrms.recruitment.repository.JobRequisitionRepository;
import com.hrms.recruitment.repository.OfferLetterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfferLetterService {

    private final OfferLetterRepository    offerLetterRepository;
    private final CandidateRepository     candidateRepository;
    private final JobRequisitionRepository requisitionRepository;
    private final ApplicationService      applicationService;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public OfferLetterDto.Response create(String tenantId, OfferLetterDto.CreateRequest req,
                                           String currentUser) {
        Application app = applicationService.getEntity(tenantId, req.getApplicationId());
        if (app.getStage() != Application.ApplicationStage.OFFER &&
            app.getStage() != Application.ApplicationStage.HR) {
            throw new BusinessException("INVALID_STAGE",
                    "Offers can only be created for applications in HR or OFFER stage. Current: "
                            + app.getStage());
        }

        OfferLetter offer = new OfferLetter();
        offer.setTenantId(tenantId);
        offer.setApplicationId(req.getApplicationId());
        offer.setOfferedCtc(req.getOfferedCtc());
        offer.setOfferedTitle(req.getOfferedTitle());
        offer.setJoiningDate(req.getJoiningDate());
        offer.setOfferExpiryDate(req.getOfferExpiryDate());
        offer.setTemplateUsed(req.getTemplateUsed());
        offer.setStatus(OfferLetter.OfferStatus.DRAFT);
        offer.setCreatedBy(currentUser);

        // Merge template fields
        String mergedContent = mergeTemplate(offer, app, tenantId);
        offer.setContent(mergedContent);

        // Advance application to OFFER stage if not already
        if (app.getStage() == Application.ApplicationStage.HR) {
            ApplicationDto.MoveStageRequest stageReq = new ApplicationDto.MoveStageRequest();
            stageReq.setStage(Application.ApplicationStage.OFFER);
            applicationService.moveStage(tenantId, app.getId(), stageReq, currentUser);
        }

        return toResponse(offerLetterRepository.save(offer));
    }

    // ── Send ──────────────────────────────────────────────────────────────────

    @Transactional
    public OfferLetterDto.Response send(String tenantId, UUID id, String currentUser) {
        OfferLetter offer = getEntity(tenantId, id);
        if (offer.getStatus() != OfferLetter.OfferStatus.DRAFT) {
            throw new BusinessException("INVALID_STATUS",
                    "Only DRAFT offers can be sent. Current: " + offer.getStatus());
        }
        offer.setStatus(OfferLetter.OfferStatus.SENT);
        offer.setSentAt(Instant.now());
        offer.setUpdatedBy(currentUser);
        return toResponse(offerLetterRepository.save(offer));
    }

    // ── Respond (accept / decline) ────────────────────────────────────────────

    @Transactional
    public OfferLetterDto.Response respond(String tenantId, UUID id,
                                            OfferLetterDto.RespondRequest req,
                                            String currentUser) {
        OfferLetter offer = getEntity(tenantId, id);
        if (offer.getStatus() != OfferLetter.OfferStatus.SENT) {
            throw new BusinessException("INVALID_STATUS",
                    "Only SENT offers can be responded to. Current: " + offer.getStatus());
        }

        offer.setStatus(req.getAccepted() ? OfferLetter.OfferStatus.ACCEPTED
                                          : OfferLetter.OfferStatus.DECLINED);
        offer.setRespondedAt(Instant.now());
        offer.setResponseNotes(req.getResponseNotes());
        offer.setUpdatedBy(currentUser);
        offerLetterRepository.save(offer);

        // Cascade to application
        ApplicationDto.MoveStageRequest stageReq = new ApplicationDto.MoveStageRequest();
        stageReq.setStage(req.getAccepted()
                ? Application.ApplicationStage.HIRED
                : Application.ApplicationStage.REJECTED);
        stageReq.setRejectionReason(req.getAccepted() ? null : "Candidate declined the offer");
        applicationService.moveStage(tenantId, offer.getApplicationId(), stageReq, currentUser);

        return toResponse(offer);
    }

    // ── Revoke ────────────────────────────────────────────────────────────────

    @Transactional
    public OfferLetterDto.Response revoke(String tenantId, UUID id, String currentUser) {
        OfferLetter offer = getEntity(tenantId, id);
        if (offer.getStatus() == OfferLetter.OfferStatus.ACCEPTED ||
            offer.getStatus() == OfferLetter.OfferStatus.REVOKED) {
            throw new BusinessException("INVALID_STATUS",
                    "Cannot revoke offer in status: " + offer.getStatus());
        }
        offer.setStatus(OfferLetter.OfferStatus.REVOKED);
        offer.setUpdatedBy(currentUser);
        return toResponse(offerLetterRepository.save(offer));
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public OfferLetterDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<OfferLetterDto.Response> listByApplication(String tenantId, UUID applicationId) {
        return offerLetterRepository
                .findByApplicationIdAndTenantIdAndDeletedFalseOrderByCreatedAtDesc(applicationId, tenantId)
                .stream().map(this::toResponse).toList();
    }

    // ── Template merge ────────────────────────────────────────────────────────

    /**
     * Replaces {{placeholders}} in the template with actual values.
     * Template keys: candidateName, offeredTitle, offeredCtc,
     *                joiningDate, offerExpiryDate, requisitionTitle
     */
    private String mergeTemplate(OfferLetter offer, Application app, String tenantId) {
        String template = offer.getTemplateUsed() != null
                ? defaultTemplate()
                : defaultTemplate();

        Candidate candidate = candidateRepository
                .findByIdAndTenantIdAndDeletedFalse(app.getCandidateId(), tenantId)
                .orElse(null);
        JobRequisition requisition = requisitionRepository
                .findByIdAndTenantIdAndDeletedFalse(app.getRequisitionId(), tenantId)
                .orElse(null);

        String candidateName = candidate != null
                ? candidate.getFirstName() + " " + candidate.getLastName() : "";
        String requisitionTitle = requisition != null ? requisition.getTitle() : "";

        return template
                .replace("{{candidateName}}",    candidateName)
                .replace("{{offeredTitle}}",      nvl(offer.getOfferedTitle()))
                .replace("{{offeredCtc}}",        nvl(offer.getOfferedCtc()))
                .replace("{{joiningDate}}",       nvl(offer.getJoiningDate()))
                .replace("{{offerExpiryDate}}",   nvl(offer.getOfferExpiryDate()))
                .replace("{{requisitionTitle}}", requisitionTitle);
    }

    private static String defaultTemplate() {
        return """
                Dear {{candidateName}},

                We are pleased to offer you the position of {{offeredTitle}}.

                Role: {{requisitionTitle}}
                Annual CTC: {{offeredCtc}}
                Joining Date: {{joiningDate}}
                Offer Valid Until: {{offerExpiryDate}}

                Please confirm your acceptance by the offer expiry date.

                Regards,
                HR Team
                """;
    }

    private static String nvl(Object v) {
        return v != null ? v.toString() : "";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private OfferLetter getEntity(String tenantId, UUID id) {
        return offerLetterRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("OfferLetter", "id", id));
    }

    private OfferLetterDto.Response toResponse(OfferLetter o) {
        return OfferLetterDto.Response.builder()
                .id(o.getId())
                .applicationId(o.getApplicationId())
                .offeredCtc(o.getOfferedCtc())
                .offeredTitle(o.getOfferedTitle())
                .joiningDate(o.getJoiningDate())
                .offerExpiryDate(o.getOfferExpiryDate())
                .templateUsed(o.getTemplateUsed())
                .content(o.getContent())
                .status(o.getStatus())
                .sentAt(o.getSentAt())
                .respondedAt(o.getRespondedAt())
                .responseNotes(o.getResponseNotes())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}
