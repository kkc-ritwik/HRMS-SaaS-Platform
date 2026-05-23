package com.hrms.recruitment.psychometric;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Vendor-agnostic gateway. Concrete implementations live in the vendor's official SDK or
 * a tiny REST adaptor. This class currently provides a stub-mode invite + webhook-handler;
 * to go live, set {@code hrms.psychometric.{vendor}.{api-key, base-url}} and replace the
 * mock invite URL with a real {@code POST /v1/test-invites}.
 *
 * Supported vendors (placeholder mode):
 *   · Mettl (mercer.com/mettl)
 *   · SHL (shl.com)
 *   · Codility, HackerEarth (engineering aptitude)
 *   · AON cut-e, AON Talent Solutions
 *
 * Wiring guide: each vendor needs (1) outbound invite, (2) inbound webhook to ingest
 * results. The webhook handler is in {@link PsychometricController#vendorWebhook}.
 */
@Slf4j
@Component
public class PsychometricVendorClient {

    @Value("${hrms.psychometric.mode:STUB}")
    private String mode;

    @Value("${hrms.psychometric.default-vendor:METTL}")
    private String defaultVendor;

    public InviteResult invite(String tenantId, String vendor, String testCode,
                               String candidateEmail, String candidateName) {
        String v = vendor == null ? defaultVendor : vendor;
        if ("STUB".equalsIgnoreCase(mode)) {
            String token = UUID.randomUUID().toString();
            String url = "https://stub.psychometric.local/" + v.toLowerCase() + "/" + token;
            log.info("[STUB-MODE] Invited {} ({}) to {} test {} → {}", candidateName, candidateEmail, v, testCode, url);
            return new InviteResult(token, url, OffsetDateTime.now().plusDays(7));
        }
        throw new UnsupportedOperationException("Live vendor integration for " + v + " not wired in this build");
    }

    /** Parse the vendor's webhook payload into a normalised result. */
    public Result ingestWebhook(String vendor, Map<String, Object> payload) {
        // Each vendor has a different schema; the controller delegates here so vendor
        // adaptors can be added without changing the API surface.
        Result r = new Result();
        r.token = (String) payload.get("token");
        Object score = payload.get("score");
        r.overallScore = score == null ? null : ((Number) score).intValue();
        Object pct = payload.get("percentile");
        r.percentile = pct == null ? null : ((Number) pct).intValue();
        r.recommendation = (String) payload.get("recommendation");
        r.reportUri = (String) payload.get("reportUrl");
        r.completedAt = OffsetDateTime.now();
        return r;
    }

    public static class InviteResult {
        public final String token;
        public final String url;
        public final OffsetDateTime expiresAt;
        public InviteResult(String t, String u, OffsetDateTime e) { this.token = t; this.url = u; this.expiresAt = e; }
    }

    public static class Result {
        public String token;
        public Integer overallScore;
        public Integer percentile;
        public String recommendation;
        public String reportUri;
        public OffsetDateTime completedAt;
    }
}
