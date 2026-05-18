package com.hrms.esign.provider;

import com.hrms.esign.model.SignatureRequest;

/** Provider-agnostic façade. One implementation per vendor (DocuSign, Adobe, Aadhaar). */
public interface ESignProvider {

    /** True if this provider matches the request's Provider enum. */
    boolean supports(SignatureRequest.Provider provider);

    /** Submit the request to the vendor, returning the external envelope id. */
    String submit(SignatureRequest req, byte[] pdfBytes);

    /** Poll vendor status (or process webhook payload) and return current status. */
    SignatureRequest.Status pollStatus(SignatureRequest req);

    /** Download the fully-signed final PDF. Returns null if not yet completed. */
    byte[] downloadSignedDocument(SignatureRequest req);

    /** Cancel/void the envelope. */
    void cancel(SignatureRequest req);
}
