package com.hrms.esign.service;

import com.hrms.esign.model.SignatureRequest;
import com.hrms.esign.provider.ESignProvider;
import com.hrms.storage.model.StoredFile;
import com.hrms.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Facade — every business service (recruitment, document, onboarding, offboarding, asset)
 * calls this. Routes to the right ESignProvider based on the request's provider field.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ESignService {

    public interface Repo extends JpaRepository<SignatureRequest, UUID> {}

    private final Repo repo;
    private final List<ESignProvider> providers;
    private final StorageService storage;

    private ESignProvider provider(SignatureRequest.Provider p) {
        return providers.stream().filter(x -> x.supports(p)).findFirst()
                .orElseThrow(() -> new IllegalStateException("No ESign provider for " + p));
    }

    @Transactional
    public SignatureRequest createAndSend(SignatureRequest req, byte[] pdfBytes) {
        ESignProvider p = provider(req.getProvider());
        String envelopeId = p.submit(req, pdfBytes);
        req.setExternalEnvelopeId(envelopeId);
        req.setStatus(SignatureRequest.Status.SENT);
        req.setSentAt(Instant.now());
        log.info("E-sign request submitted via {}: {} ({})", req.getProvider(), req.getTitle(), envelopeId);
        return repo.save(req);
    }

    @Transactional
    public SignatureRequest poll(UUID id) throws IOException {
        SignatureRequest req = repo.findById(id).orElseThrow();
        if (req.getStatus() == SignatureRequest.Status.COMPLETED) return req;
        ESignProvider p = provider(req.getProvider());
        SignatureRequest.Status now = p.pollStatus(req);
        if (now == SignatureRequest.Status.COMPLETED && req.getSignedDocumentUri() == null) {
            byte[] signed = p.downloadSignedDocument(req);
            if (signed != null) {
                StoredFile sf = storage.upload(req.getTenantId(),
                        "esign/signed/" + req.getSubjectType(),
                        req.getTitle() + "-signed.pdf", "application/pdf",
                        new ByteArrayInputStream(signed), signed.length);
                req.setSignedDocumentUri(sf.getStorageUri());
                req.setCompletedAt(Instant.now());
            }
        }
        req.setStatus(now);
        return repo.save(req);
    }

    @Transactional
    public SignatureRequest cancel(UUID id) {
        SignatureRequest req = repo.findById(id).orElseThrow();
        provider(req.getProvider()).cancel(req);
        req.setStatus(SignatureRequest.Status.CANCELLED);
        req.setCancelledAt(Instant.now());
        return repo.save(req);
    }

    public SignatureRequest get(UUID id) { return repo.findById(id).orElseThrow(); }
}
