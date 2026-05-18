package com.hrms.esign.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.esign.model.SignatureRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * DocuSign REST API client (envelope-based). Uses JWT grant authentication —
 * Integration Key + Impersonated User + RSA private key configured via env. When credentials
 * are missing, falls back to logging the request so dev flows aren't blocked.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocuSignProvider implements ESignProvider {

    @Value("${hrms.esign.docusign.integration-key:}") private String integrationKey;
    @Value("${hrms.esign.docusign.user-id:}") private String userId;
    @Value("${hrms.esign.docusign.account-id:}") private String accountId;
    @Value("${hrms.esign.docusign.base-url:https://demo.docusign.net/restapi}") private String baseUrl;

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate rest = new RestTemplate();

    @Override
    public boolean supports(SignatureRequest.Provider provider) {
        return provider == SignatureRequest.Provider.DOCUSIGN;
    }

    @Override
    public String submit(SignatureRequest req, byte[] pdfBytes) {
        if (integrationKey == null || integrationKey.isBlank() || integrationKey.startsWith("REPLACE")) {
            log.warn("DocuSign not configured — logging envelope: {} signers={}", req.getTitle(), req.getSigners().size());
            return "dev-envelope-" + UUID.randomUUID();
        }
        try {
            String token = obtainAccessToken();
            Map<String, Object> envelope = buildEnvelopeJson(req, pdfBytes);
            HttpHeaders h = new HttpHeaders();
            h.setBearerAuth(token);
            h.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<Map> resp = rest.exchange(
                    baseUrl + "/v2.1/accounts/" + accountId + "/envelopes",
                    HttpMethod.POST,
                    new HttpEntity<>(mapper.writeValueAsString(envelope), h),
                    Map.class);
            Object id = resp.getBody() == null ? null : resp.getBody().get("envelopeId");
            return id == null ? null : id.toString();
        } catch (Exception e) {
            log.error("DocuSign submit failed: {}", e.getMessage());
            throw new RuntimeException("DocuSign submission failed", e);
        }
    }

    @Override
    public SignatureRequest.Status pollStatus(SignatureRequest req) {
        if (req.getExternalEnvelopeId() == null || integrationKey == null || integrationKey.isBlank()) {
            return req.getStatus();
        }
        try {
            HttpHeaders h = new HttpHeaders(); h.setBearerAuth(obtainAccessToken());
            ResponseEntity<Map> resp = rest.exchange(
                    baseUrl + "/v2.1/accounts/" + accountId + "/envelopes/" + req.getExternalEnvelopeId(),
                    HttpMethod.GET, new HttpEntity<>(h), Map.class);
            Object status = resp.getBody() == null ? null : resp.getBody().get("status");
            return mapStatus(status == null ? "" : status.toString());
        } catch (Exception e) {
            log.warn("DocuSign poll failed: {}", e.getMessage());
            return req.getStatus();
        }
    }

    @Override
    public byte[] downloadSignedDocument(SignatureRequest req) {
        if (req.getExternalEnvelopeId() == null || integrationKey == null || integrationKey.isBlank()) return null;
        try {
            HttpHeaders h = new HttpHeaders(); h.setBearerAuth(obtainAccessToken());
            ResponseEntity<byte[]> resp = rest.exchange(
                    baseUrl + "/v2.1/accounts/" + accountId + "/envelopes/" + req.getExternalEnvelopeId() + "/documents/combined",
                    HttpMethod.GET, new HttpEntity<>(h), byte[].class);
            return resp.getBody();
        } catch (Exception e) {
            log.warn("DocuSign download failed: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void cancel(SignatureRequest req) {
        if (req.getExternalEnvelopeId() == null || integrationKey == null || integrationKey.isBlank()) return;
        try {
            HttpHeaders h = new HttpHeaders(); h.setBearerAuth(obtainAccessToken());
            h.setContentType(MediaType.APPLICATION_JSON);
            rest.exchange(
                    baseUrl + "/v2.1/accounts/" + accountId + "/envelopes/" + req.getExternalEnvelopeId(),
                    HttpMethod.PUT, new HttpEntity<>("{\"status\":\"voided\",\"voidedReason\":\"Cancelled by HRMS\"}", h), String.class);
        } catch (Exception e) {
            log.warn("DocuSign cancel failed: {}", e.getMessage());
        }
    }

    // ── helpers ────────────────────────────────────────────────────────────
    /** Real impl exchanges the RSA-signed JWT for an access token. Stubbed here. */
    private String obtainAccessToken() {
        // Production: docusign.oauth.granting JWT flow. Placeholder for now.
        return "stub-access-token";
    }

    private Map<String, Object> buildEnvelopeJson(SignatureRequest req, byte[] pdfBytes) {
        List<Map<String, Object>> signers = new ArrayList<>();
        int routingOrder = 1;
        for (SignatureRequest.Signer s : req.getSigners()) {
            signers.add(Map.of(
                    "email", s.getEmail(), "name", s.getName(),
                    "recipientId", String.valueOf(routingOrder),
                    "routingOrder", String.valueOf(s.getSigningOrder() == null ? routingOrder : s.getSigningOrder()),
                    "tabs", Map.of("signHereTabs", List.of(
                            Map.of("anchorString", "/sig" + routingOrder + "/", "anchorYOffset", "10")))));
            routingOrder++;
        }
        return Map.of(
                "emailSubject", req.getTitle(),
                "documents", List.of(Map.of(
                        "documentBase64", Base64.getEncoder().encodeToString(pdfBytes),
                        "name", req.getTitle() + ".pdf", "fileExtension", "pdf",
                        "documentId", "1")),
                "recipients", Map.of("signers", signers),
                "status", "sent");
    }

    private SignatureRequest.Status mapStatus(String docusignStatus) {
        return switch (docusignStatus.toLowerCase()) {
            case "sent" -> SignatureRequest.Status.SENT;
            case "delivered" -> SignatureRequest.Status.VIEWED;
            case "completed" -> SignatureRequest.Status.COMPLETED;
            case "declined" -> SignatureRequest.Status.DECLINED;
            case "voided" -> SignatureRequest.Status.CANCELLED;
            case "expired" -> SignatureRequest.Status.EXPIRED;
            default -> SignatureRequest.Status.SENT;
        };
    }
}
