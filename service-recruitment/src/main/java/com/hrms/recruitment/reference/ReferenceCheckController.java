package com.hrms.recruitment.reference;

import com.hrms.mail.model.MailRequest;
import com.hrms.mail.service.MailService;
import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recruitment/references")
@RequiredArgsConstructor
public class ReferenceCheckController {

    public interface Repo extends JpaRepository<ReferenceCheck, UUID> {
        List<ReferenceCheck> findByTenantIdAndCandidateId(String tenantId, UUID candidateId);
        java.util.Optional<ReferenceCheck> findByRequestToken(String requestToken);
    }

    private final Repo repo;
    private final MailService mailService;

    @Value("${hrms.recruitment.reference.frontend-url:${FRONTEND_BASE_URL:http://localhost:5173}}")
    private String frontendUrl;

    @PostMapping
    @Transactional
    public ReferenceCheck create(@RequestBody ReferenceCheck r) {
        r.setTenantId(TenantContext.get());
        r.setRequestToken(UUID.randomUUID().toString().replace("-", ""));
        r.setStatus(ReferenceCheck.Status.PENDING);
        return repo.save(r);
    }

    @PostMapping("/{id}/invite")
    @Transactional
    public ReferenceCheck invite(@PathVariable UUID id) {
        ReferenceCheck r = repo.findById(id).orElseThrow();
        r.setStatus(ReferenceCheck.Status.INVITED);
        r.setRequestedAt(Instant.now());
        if (r.getRefereeEmail() != null && !r.getRefereeEmail().isBlank()) {
            String url = frontendUrl + "/reference-check/" + r.getRequestToken();
            mailService.sendAsync(MailRequest.builder()
                    .to(r.getRefereeEmail())
                    .subject("Reference request for our candidate")
                    .html("""
                          <p>Hi %s,</p>
                          <p>Our candidate has listed you as a professional reference.
                             Could you spare 5 minutes to answer a short set of questions?</p>
                          <p><a href='%s'>Open reference form</a></p>
                          <p>Thank you for your time.</p>
                          """.formatted(r.getRefereeName(), url))
                    .category("recruitment-reference")
                    .build());
        }
        return repo.save(r);
    }

    /** Referee-facing endpoint — uses one-shot token, no auth required. */
    @PostMapping("/respond/{token}")
    @Transactional
    public Map<String, Object> respond(@PathVariable String token, @RequestBody RespondRequest body) {
        ReferenceCheck r = repo.findByRequestToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        if (r.getStatus() == ReferenceCheck.Status.RESPONDED) {
            return Map.of("status", "already_responded");
        }
        r.setStructuredFeedback(body.structured());
        r.setFreeTextFeedback(body.freeText());
        r.setWouldRehire(body.wouldRehire());
        r.setOverallRating(body.overallRating());
        r.setStatus(ReferenceCheck.Status.RESPONDED);
        r.setRespondedAt(Instant.now());
        repo.save(r);
        return Map.of("status", "thanks");
    }

    @GetMapping("/candidate/{candidateId}")
    public List<ReferenceCheck> byCandidate(@PathVariable UUID candidateId) {
        return repo.findByTenantIdAndCandidateId(TenantContext.get(), candidateId);
    }

    public record RespondRequest(Map<String, Object> structured, String freeText,
                                  Boolean wouldRehire, Integer overallRating) {}
}
