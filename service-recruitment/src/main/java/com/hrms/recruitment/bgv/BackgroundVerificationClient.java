package com.hrms.recruitment.bgv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

/**
 * Vendor-agnostic BGV client. Configured via hrms.recruitment.bgv.provider (AUTHBRIDGE / ONGRID / CHECKR).
 * When provider creds are missing falls back to logging — keeps dev flows unblocked.
 *
 * Typical flow:
 *   1. initiate(candidate) → returns vendor-side ID
 *   2. webhook callback (or poll) updates VerificationCase row
 *   3. UI shows final report URL + status
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BackgroundVerificationClient {

    @Value("${hrms.recruitment.bgv.provider:authbridge}") private String provider;
    @Value("${hrms.recruitment.bgv.base-url:https://api.authbridge.com}") private String baseUrl;
    @Value("${hrms.recruitment.bgv.api-key:}") private String apiKey;

    private final RestTemplate rest = new RestTemplate();

    public InitiateResult initiate(InitiateRequest req) {
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("REPLACE")) {
            log.warn("BGV provider not configured — logging request for candidate {}", req.candidateId());
            return new InitiateResult("dev-bgv-" + UUID.randomUUID(), "QUEUED", null);
        }
        try {
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_JSON);
            h.set("X-API-Key", apiKey);
            Map<String, Object> payload = Map.of(
                    "candidateName", req.candidateName(),
                    "email", req.candidateEmail(),
                    "phone", req.candidatePhone() == null ? "" : req.candidatePhone(),
                    "pan", req.pan() == null ? "" : req.pan(),
                    "aadhaar", req.aadhaar() == null ? "" : req.aadhaar(),
                    "packageType", req.packageType(),       // BASIC / ADVANCED / EXECUTIVE
                    "callbackUrl", req.callbackUrl(),
                    "checks", req.checks());                 // EMPLOYMENT, EDUCATION, COURT, REFERENCE, DRUG, ID
            ResponseEntity<Map> resp = rest.exchange(
                    baseUrl + "/v1/verification/initiate",
                    HttpMethod.POST, new HttpEntity<>(payload, h), Map.class);
            Object id = resp.getBody() == null ? null : resp.getBody().get("verificationId");
            Object url = resp.getBody() == null ? null : resp.getBody().get("trackingUrl");
            return new InitiateResult(id == null ? null : id.toString(), "INITIATED",
                    url == null ? null : url.toString());
        } catch (Exception e) {
            log.error("BGV initiate failed for {}: {}", req.candidateId(), e.getMessage());
            return new InitiateResult(null, "FAILED", e.getMessage());
        }
    }

    public Result pollStatus(String externalId) {
        if (externalId == null || apiKey == null || apiKey.isBlank() || apiKey.startsWith("REPLACE")) {
            return new Result(externalId, "UNKNOWN", null, null, null);
        }
        try {
            HttpHeaders h = new HttpHeaders(); h.set("X-API-Key", apiKey);
            ResponseEntity<Map> resp = rest.exchange(
                    baseUrl + "/v1/verification/" + externalId,
                    HttpMethod.GET, new HttpEntity<>(h), Map.class);
            Map<String, Object> body = resp.getBody() == null ? Map.of() : resp.getBody();
            return new Result(externalId,
                    str(body.get("status")),
                    str(body.get("overallResult")),     // CLEAR / DISCREPANT / INSUFFICIENT
                    str(body.get("reportUrl")),
                    body);
        } catch (Exception e) {
            log.warn("BGV poll failed: {}", e.getMessage());
            return new Result(externalId, "ERROR", null, null, null);
        }
    }

    private String str(Object v) { return v == null ? null : v.toString(); }

    public record InitiateRequest(UUID candidateId, String candidateName, String candidateEmail,
                                  String candidatePhone, String pan, String aadhaar,
                                  String packageType, String callbackUrl, java.util.List<String> checks) {}
    public record InitiateResult(String externalVerificationId, String status, String trackingUrl) {}
    public record Result(String externalVerificationId, String status, String overallResult,
                         String reportUrl, Map<String, Object> rawResponse) {}
}
