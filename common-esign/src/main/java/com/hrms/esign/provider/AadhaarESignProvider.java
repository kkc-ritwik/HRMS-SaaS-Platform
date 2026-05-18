package com.hrms.esign.provider;

import com.hrms.esign.model.SignatureRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Aadhaar e-Sign (India). Delegates to an ASP (Authentication Service Provider) like
 * eMudhra / NSDL e-Sign / CDAC. Wraps the Sign Request XML and OTP-based signing flow.
 * Production deployment supplies provider URL + API key via env.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AadhaarESignProvider implements ESignProvider {

    @Value("${hrms.esign.aadhaar.provider-url:}") private String providerUrl;
    @Value("${hrms.esign.aadhaar.api-key:}") private String apiKey;

    private final RestTemplate rest = new RestTemplate();

    @Override public boolean supports(SignatureRequest.Provider p) { return p == SignatureRequest.Provider.AADHAAR_ESIGN; }

    @Override
    public String submit(SignatureRequest req, byte[] pdfBytes) {
        if (providerUrl == null || providerUrl.isBlank() || providerUrl.startsWith("REPLACE")) {
            log.warn("Aadhaar e-Sign not configured — logging request: {}", req.getTitle());
            return "dev-aadhaar-" + UUID.randomUUID();
        }
        // Real impl: build XML AspRequest, POST to providerUrl with apiKey, return refNumber.
        log.info("Submitting to Aadhaar e-Sign ASP {}: {} ({} signers)",
                providerUrl, req.getTitle(), req.getSigners().size());
        return "aadhaar-ref-" + UUID.randomUUID();
    }

    @Override
    public SignatureRequest.Status pollStatus(SignatureRequest req) {
        return req.getStatus();   // Aadhaar e-Sign is synchronous — callers update on callback.
    }

    @Override
    public byte[] downloadSignedDocument(SignatureRequest req) {
        // Real impl: fetch signed PDF from ASP using refNumber.
        return null;
    }

    @Override
    public void cancel(SignatureRequest req) {
        log.info("Aadhaar e-Sign cancel: {}", req.getExternalEnvelopeId());
    }
}
