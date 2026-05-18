package com.hrms.document.versioning;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Immutable historical snapshot of a Document's content. Each upload of an existing
 * Document creates a new DocumentVersion with version_number = max(prev)+1; the
 * Document.file_url always points to the latest version's storage URI.
 *
 * Designed for compliance audit ("what was the policy text the employee acknowledged on
 * 2024-03-12?") and rollback ("revert to v3").
 */
@Entity
@Table(name = "document_versions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","document_id","version_number"}),
        indexes = @Index(name = "ix_docver_doc", columnList = "document_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DocumentVersion extends BaseEntity {

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "storage_uri", length = 1000, nullable = false)
    private String storageUri;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "checksum_sha256", length = 64)
    private String checksumSha256;

    @Column(name = "content_type", length = 100)
    private String contentType;

    /** Free-form description ("Initial upload" / "Updated reimbursement caps" / etc.). */
    @Column(name = "change_summary", length = 1000)
    private String changeSummary;

    @Column(name = "uploaded_by")
    private UUID uploadedBy;

    @Column(name = "is_current", nullable = false)
    private boolean current = false;
}
