package com.hrms.cases.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "case_notes", indexes = @Index(name = "ix_note_case", columnList = "case_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Auditable("CaseNote")
@EntityListeners(AuditEntityListener.class)
public class CaseNote {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "case_id", nullable = false) private UUID caseId;
    @Column(name = "author_id", nullable = false) private UUID authorId;
    @Column(length = 5000, nullable = false) private String content;
    /** Internal notes invisible to complainant. */
    @Column(name = "is_internal") private Boolean isInternal;
    /** Evidence file URIs stored in object storage. */
    @Column(name = "attachment_uri", length = 1000) private String attachmentUri;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;

    @PrePersist void prePersist() { createdAt = OffsetDateTime.now(); }
}
