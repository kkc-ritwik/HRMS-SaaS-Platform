package com.hrms.esign.provider;

import com.hrms.esign.model.SignatureRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Built-in lightweight e-sign — captures a typed signature image (data URI) from the
 * signer's browser, stamps it onto the PDF, no external provider. Sufficient for low-stakes
 * acknowledgements (handbook ack, asset handover); for legally-binding contracts use DocuSign
 * or Aadhaar e-Sign.
 */
@Slf4j
@Component
public class InternalESignProvider implements ESignProvider {

    @Override public boolean supports(SignatureRequest.Provider p) { return p == SignatureRequest.Provider.INTERNAL; }

    @Override
    public String submit(SignatureRequest req, byte[] pdfBytes) {
        return "internal-" + UUID.randomUUID();
    }

    @Override public SignatureRequest.Status pollStatus(SignatureRequest req) { return req.getStatus(); }

    @Override
    public byte[] downloadSignedDocument(SignatureRequest req) {
        // Signed pdf is stamped + stored separately at signedDocumentUri when each signer completes.
        return null;
    }

    @Override public void cancel(SignatureRequest req) { /* no-op */ }
}
