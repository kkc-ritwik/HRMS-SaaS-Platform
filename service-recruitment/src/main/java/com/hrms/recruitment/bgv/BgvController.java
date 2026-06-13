package com.hrms.recruitment.bgv;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/recruitment/bgv")
@RequiredArgsConstructor
public class BgvController {

    public interface Repo extends JpaRepository<VerificationCase, UUID> {
        Optional<VerificationCase> findByTenantIdAndCandidateId(String tenantId, UUID candidateId);
        Optional<VerificationCase> findByExternalVerificationId(String externalVerificationId);
        List<VerificationCase> findByTenantIdOrderByInitiatedAtDesc(String tenantId);
    }

    private final Repo repo;
    private final BackgroundVerificationClient client;

    /** List all BGV cases for the current tenant. */
    @GetMapping
    public List<VerificationCase> list() {
        return repo.findByTenantIdOrderByInitiatedAtDesc(TenantContext.get());
    }

    /** Shared secret for HMAC-SHA256 verification of vendor webhook bodies. */
    @Value("${hrms.recruitment.bgv.webhook-secret:}")
    private String webhookSecret;

    @PostMapping("/initiate")
    @Transactional
    public VerificationCase initiate(@RequestBody BackgroundVerificationClient.InitiateRequest req) {
        BackgroundVerificationClient.InitiateResult r = client.initiate(req);
        VerificationCase vc = repo.findByTenantIdAndCandidateId(TenantContext.get(), req.candidateId())
                .orElseGet(VerificationCase::new);
        vc.setTenantId(TenantContext.get());
        vc.setCandidateId(req.candidateId());
        vc.setExternalVerificationId(r.externalVerificationId());
        vc.setStatus(r.status());
        vc.setTrackingUrl(r.trackingUrl());
        vc.setPackageType(req.packageType());
        vc.setInitiatedAt(Instant.now());
        return repo.save(vc);
    }

    @PostMapping("/{caseId}/refresh")
    @Transactional
    public VerificationCase refresh(@PathVariable UUID caseId) {
        VerificationCase vc = repo.findById(caseId).orElseThrow();
        BackgroundVerificationClient.Result r = client.pollStatus(vc.getExternalVerificationId());
        vc.setStatus(r.status());
        vc.setOverallResult(r.overallResult());
        vc.setReportUrl(r.reportUrl());
        vc.setVendorResponse(r.rawResponse());
        if ("CLEAR".equalsIgnoreCase(r.overallResult()) || "DISCREPANT".equalsIgnoreCase(r.overallResult())) {
            vc.setCompletedAt(Instant.now());
        }
        return repo.save(vc);
    }

    /**
     * Vendor webhook target — they POST status updates here.
     * Verifies HMAC-SHA256 of the raw body against {@code X-BGV-Signature: sha256=<hex>} header.
     * If hrms.recruitment.bgv.webhook-secret is unset (dev), verification is skipped with a WARN.
     */
    @PostMapping("/webhook/{provider}")
    @Transactional
    public ResponseEntity<Map<String, String>> webhook(@PathVariable String provider,
                                                        @RequestHeader(value = "X-BGV-Signature", required = false) String signature,
                                                        @RequestBody String rawBody) {
        if (!verifySignature(rawBody, signature)) {
            log.warn("BGV webhook signature mismatch from {}", provider);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "rejected", "reason", "bad signature"));
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(rawBody, Map.class);
            Object ext = body.get("verificationId");
            if (ext != null) {
                repo.findByExternalVerificationId(ext.toString()).ifPresent(vc -> {
                    vc.setStatus(String.valueOf(body.getOrDefault("status", vc.getStatus())));
                    vc.setOverallResult(String.valueOf(body.getOrDefault("overallResult", vc.getOverallResult())));
                    vc.setVendorResponse(body);
                    repo.save(vc);
                });
            }
            return ResponseEntity.ok(Map.of("status", "ack"));
        } catch (Exception e) {
            log.warn("BGV webhook parse error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("status", "error", "reason", e.getMessage()));
        }
    }

    private boolean verifySignature(String body, String signatureHeader) {
        if (webhookSecret == null || webhookSecret.isBlank() || webhookSecret.startsWith("REPLACE")) {
            log.warn("BGV webhook secret not configured — accepting unverified payload (dev only)");
            return true;
        }
        if (signatureHeader == null) return false;
        String provided = signatureHeader.startsWith("sha256=") ? signatureHeader.substring(7) : signatureHeader;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
            return constantTimeEquals(provided, expected);
        } catch (Exception e) { return false; }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int r = 0;
        for (int i = 0; i < a.length(); i++) r |= a.charAt(i) ^ b.charAt(i);
        return r == 0;
    }

    @GetMapping("/candidate/{candidateId}")
    public List<VerificationCase> ofCandidate(@PathVariable UUID candidateId) {
        return repo.findByTenantIdAndCandidateId(TenantContext.get(), candidateId)
                .map(List::of).orElse(List.of());
    }
}
