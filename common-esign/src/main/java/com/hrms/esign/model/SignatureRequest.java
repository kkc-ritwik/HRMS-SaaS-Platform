package com.hrms.esign.model;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Tracks an outbound document for signature â€” works for offer letters, NDAs, policy
 * acknowledgements, asset handover forms, separation letters, salary revision letters, etc.
 * Provider-agnostic: DOCUSIGN / ADOBE_SIGN / AADHAAR_ESIGN / INTERNAL.
 */
@Entity
@Table(name = "signature_requests",
        indexes = {
            @Index(name = "ix_sigreq_status", columnList = "status"),
            @Index(name = "ix_sigreq_subject", columnList = "subject_type,subject_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SignatureRequest extends BaseEntity {

    /** What we're getting signed: OFFER_LETTER / POLICY / NDA / ASSET_HANDOVER / SEPARATION / OTHER */
    @Column(name = "subject_type", length = 50, nullable = false)
    private String subjectType;

    /** The id of the underlying business object (e.g. OfferLetter.id). */
    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(name = "document_storage_uri", length = 1000, nullable = false)
    private String documentStorageUri;

    @Column(name = "title", length = 300, nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 30, nullable = false)
    private Provider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status = Status.DRAFT;

    /** Provider-side envelope/request id once submitted. */
    @Column(name = "external_envelope_id", length = 200)
    private String externalEnvelopeId;

    /** Final, fully-signed PDF stored back to MinIO once provider returns COMPLETED. */
    @Column(name = "signed_document_uri", length = 1000)
    private String signedDocumentUri;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "signers", columnDefinition = "jsonb", nullable = false)
    private List<Signer> signers;

    @Column(name = "expires_at") private Instant expiresAt;
    @Column(name = "sent_at") private Instant sentAt;
    @Column(name = "completed_at") private Instant completedAt;
    @Column(name = "cancelled_at") private Instant cancelledAt;
    @Column(name = "last_event", length = 500) private String lastEvent;

    public enum Provider { DOCUSIGN, ADOBE_SIGN, AADHAAR_ESIGN, INTERNAL }
    public enum Status { DRAFT, SENT, VIEWED, PARTIALLY_SIGNED, COMPLETED, DECLINED, EXPIRED, CANCELLED, FAILED }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Signer {
        private String name;
        private String email;
        private Integer signingOrder;     // 1, 2, 3... null = anyone
        private String role;              // EMPLOYEE / MANAGER / HR / WITNESS
        private String status;            // PENDING / SIGNED / DECLINED
        private Instant signedAt;
        private String signatureImageDataUri;
    }
}
