package com.hrms.recruitment.psychometric;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/recruitment/psychometric")
@RequiredArgsConstructor
public class PsychometricController {

    public interface Repo extends JpaRepository<PsychometricAssessment, UUID> {
        Optional<PsychometricAssessment> findByInviteToken(String token);

        @Query("SELECT p FROM PsychometricAssessment p WHERE p.tenantId = :t AND p.candidateId = :c ORDER BY p.invitedAt DESC")
        List<PsychometricAssessment> forCandidate(@Param("t") String t, @Param("c") UUID c);
    }

    private final Repo repo;
    private final PsychometricVendorClient client;

    @PostMapping("/invite")
    @Transactional
    public PsychometricAssessment invite(@RequestBody Map<String, Object> body) {
        String tenant = TenantContext.get();
        PsychometricAssessment a = new PsychometricAssessment();
        a.setTenantId(tenant);
        a.setCandidateId(UUID.fromString((String) body.get("candidateId")));
        if (body.get("applicationId") != null) a.setApplicationId(UUID.fromString((String) body.get("applicationId")));
        a.setVendor((String) body.getOrDefault("vendor", "METTL"));
        a.setTestCode((String) body.get("testCode"));
        a.setTestName((String) body.get("testName"));

        PsychometricVendorClient.InviteResult inv = client.invite(
                tenant, a.getVendor(), a.getTestCode(),
                (String) body.get("candidateEmail"), (String) body.get("candidateName"));
        a.setInviteToken(inv.token);
        a.setInviteUrl(inv.url);
        a.setInvitedAt(OffsetDateTime.now());
        a.setExpiresAt(inv.expiresAt);
        return repo.save(a);
    }

    @PostMapping("/webhook/{vendor}")
    @Transactional
    public Map<String, Object> vendorWebhook(@PathVariable String vendor, @RequestBody Map<String, Object> payload) {
        PsychometricVendorClient.Result r = client.ingestWebhook(vendor, payload);
        PsychometricAssessment a = repo.findByInviteToken(r.token)
                .orElseThrow(() -> new IllegalArgumentException("Unknown invite token"));
        a.setOverallScore(r.overallScore);
        a.setPercentile(r.percentile);
        a.setRecommendation(r.recommendation);
        a.setReportUri(r.reportUri);
        a.setCompletedAt(r.completedAt);
        a.setStatus(PsychometricAssessment.Status.COMPLETED);
        a.setRawPayload(payload);
        repo.save(a);
        return Map.of("status", "INGESTED", "assessmentId", a.getId());
    }

    @GetMapping("/candidate/{candidateId}")
    public List<PsychometricAssessment> forCandidate(@PathVariable UUID candidateId) {
        return repo.forCandidate(TenantContext.get(), candidateId);
    }
}
