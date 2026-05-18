package com.hrms.document.versioning;

import com.hrms.security.model.TenantContext;
import com.hrms.storage.model.StoredFile;
import com.hrms.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentVersionService {

    public interface VersionRepo extends JpaRepository<DocumentVersion, UUID> {
        List<DocumentVersion> findByTenantIdAndDocumentIdOrderByVersionNumberDesc(String tenantId, UUID documentId);
        Optional<DocumentVersion> findByTenantIdAndDocumentIdAndCurrentTrue(String tenantId, UUID documentId);
        Optional<DocumentVersion> findFirstByTenantIdAndDocumentIdOrderByVersionNumberDesc(String tenantId, UUID documentId);
    }

    private final VersionRepo repo;
    private final StorageService storage;

    /** Upload a new version of an existing document. Returns the new version. */
    @Transactional
    public DocumentVersion uploadNewVersion(UUID documentId, MultipartFile file, String changeSummary, UUID uploadedBy) throws IOException {
        String tenant = TenantContext.get();
        int nextVersion = repo.findFirstByTenantIdAndDocumentIdOrderByVersionNumberDesc(tenant, documentId)
                .map(v -> v.getVersionNumber() + 1).orElse(1);

        StoredFile sf = storage.upload(tenant, "document-versions/" + documentId + "/v" + nextVersion,
                file.getOriginalFilename(), file.getContentType(),
                file.getInputStream(), file.getSize());

        // demote the prior current version
        repo.findByTenantIdAndDocumentIdAndCurrentTrue(tenant, documentId)
                .ifPresent(prev -> { prev.setCurrent(false); repo.save(prev); });

        DocumentVersion v = new DocumentVersion();
        v.setTenantId(tenant);
        v.setDocumentId(documentId);
        v.setVersionNumber(nextVersion);
        v.setStorageUri(sf.getStorageUri());
        v.setSizeBytes(sf.getSizeBytes());
        v.setChecksumSha256(sf.getEtag());
        v.setContentType(sf.getContentType());
        v.setChangeSummary(changeSummary);
        v.setUploadedBy(uploadedBy);
        v.setCurrent(true);
        DocumentVersion saved = repo.save(v);
        log.info("Document {} → uploaded v{}", documentId, nextVersion);
        return saved;
    }

    public List<DocumentVersion> history(UUID documentId) {
        return repo.findByTenantIdAndDocumentIdOrderByVersionNumberDesc(TenantContext.get(), documentId);
    }

    /** Make a prior version current (rollback). */
    @Transactional
    public DocumentVersion revertTo(UUID documentId, int versionNumber) {
        String tenant = TenantContext.get();
        DocumentVersion target = repo.findByTenantIdAndDocumentIdOrderByVersionNumberDesc(tenant, documentId).stream()
                .filter(v -> v.getVersionNumber() == versionNumber)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Version not found"));
        repo.findByTenantIdAndDocumentIdAndCurrentTrue(tenant, documentId)
                .ifPresent(prev -> { prev.setCurrent(false); repo.save(prev); });
        target.setCurrent(true);
        return repo.save(target);
    }
}
