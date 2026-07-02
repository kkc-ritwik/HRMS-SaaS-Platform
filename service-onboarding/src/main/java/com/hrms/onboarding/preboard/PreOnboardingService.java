package com.hrms.onboarding.preboard;

import com.hrms.mail.model.MailRequest;
import com.hrms.mail.service.MailService;
import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PreOnboardingService {

    public interface PortalRepo extends JpaRepository<PreOnboardingPortal, UUID> {
        Optional<PreOnboardingPortal> findByTenantIdAndCandidateId(String tenantId, UUID candidateId);
        Optional<PreOnboardingPortal> findByAccessToken(String accessToken);
    }

    private final PortalRepo portals;
    private final MailService mailService;

    @Value("${hrms.onboarding.preboard.base-url:${FRONTEND_BASE_URL:http://localhost:5173}}")
    private String baseUrl;

    @Transactional
    public PreOnboardingPortal createForCandidate(UUID candidateId, UUID offerId,
                                                   String candidateName, String candidateEmail,
                                                   LocalDate proposedJoinDate) {
        PreOnboardingPortal portal = portals.findByTenantIdAndCandidateId(TenantContext.get(), candidateId)
                .orElseGet(PreOnboardingPortal::new);
        if (portal.getTenantId() == null) portal.setTenantId(TenantContext.get());
        portal.setCandidateId(candidateId);
        portal.setOfferId(offerId);
        portal.setProposedJoinDate(proposedJoinDate);
        if (portal.getAccessToken() == null) {
            portal.setAccessToken(UUID.randomUUID().toString().replace("-", ""));
        }
        portal.setStatus(PreOnboardingPortal.Status.INVITED);
        portal.setInvitationSentAt(Instant.now());
        if (portal.getUploadedDocuments() == null) portal.setUploadedDocuments(new HashMap<>());
        PreOnboardingPortal saved = portals.save(portal);
        sendInvitationEmail(saved, candidateName, candidateEmail);
        return saved;
    }

    @Transactional
    public PreOnboardingPortal uploadDocument(String token, String documentType, String storageUri) {
        PreOnboardingPortal p = portals.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid access token"));
        Map<String, String> docs = p.getUploadedDocuments() == null
                ? new HashMap<>() : new HashMap<>(p.getUploadedDocuments());
        docs.put(documentType, storageUri);
        p.setUploadedDocuments(docs);
        p.setStatus(PreOnboardingPortal.Status.IN_PROGRESS);
        return portals.save(p);
    }

    @Transactional
    public PreOnboardingPortal savePersonalDetails(String token, Map<String, Object> details) {
        PreOnboardingPortal p = portals.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid access token"));
        p.setPersonalDetails(details);
        p.setStatus(PreOnboardingPortal.Status.IN_PROGRESS);
        return portals.save(p);
    }

    @Transactional
    public PreOnboardingPortal acknowledgeHandbook(String token) {
        PreOnboardingPortal p = portals.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid access token"));
        p.setHandbookAcknowledgedAt(Instant.now());
        return portals.save(p);
    }

    @Transactional
    public PreOnboardingPortal complete(String token) {
        PreOnboardingPortal p = portals.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid access token"));
        p.setStatus(PreOnboardingPortal.Status.COMPLETED);
        p.setCompletedAt(Instant.now());
        return portals.save(p);
    }

    public PreOnboardingPortal byToken(String token) {
        return portals.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid access token"));
    }

    private void sendInvitationEmail(PreOnboardingPortal p, String name, String email) {
        if (email == null || email.isBlank()) return;
        String url = baseUrl + "/preonboarding/" + p.getAccessToken();
        mailService.sendAsync(MailRequest.builder()
                .to(email)
                .subject("Welcome — your pre-joining portal is ready")
                .html("""
                      <p>Hi %s,</p>
                      <p>Congratulations! Your portal to complete pre-joining formalities is ready.
                         Please complete the steps before your start date <strong>%s</strong>.</p>
                      <p><a href="%s">Open your pre-onboarding portal</a></p>
                      <p>This link is unique to you — please don't share it.</p>
                      """.formatted(name == null ? "" : name,
                                    p.getProposedJoinDate() == null ? "TBD" : p.getProposedJoinDate(),
                                    url))
                .category("preonboarding-invite")
                .build());
    }
}
