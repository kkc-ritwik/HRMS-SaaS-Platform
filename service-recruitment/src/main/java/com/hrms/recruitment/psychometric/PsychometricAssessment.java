package com.hrms.recruitment.psychometric;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Psychometric / aptitude assessment record. Stub-level integration: this entity captures
 * the invite, the resulting score sheet, and the raw vendor payload. Concrete vendor
 * adaptors (Mettl, SHL, HackerEarth, Codility, AON) plug in via
 * {@link PsychometricVendorClient}.
 */
@Entity
@Table(name = "recruit_psychometric_assessments", indexes = {
        @Index(name = "ix_psych_candidate", columnList = "tenant_id,candidate_id"),
        @Index(name = "ix_psych_invite", columnList = "invite_token")
})
@Auditable(value = "PsychometricAssessment", redactFields = "rawPayload")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PsychometricAssessment extends BaseEntity {

    @Column(name = "candidate_id", nullable = false) private UUID candidateId;
    @Column(name = "application_id") private UUID applicationId;
    @Column(name = "vendor", length = 30, nullable = false) private String vendor;   // METTL / SHL / CODILITY / HACKEREARTH / AON / INTERNAL
    @Column(name = "test_code", length = 100, nullable = false) private String testCode;
    @Column(name = "test_name", length = 200) private String testName;

    @Column(name = "invite_token", length = 200, unique = true) private String inviteToken;
    @Column(name = "invite_url", length = 1000) private String inviteUrl;
    @Column(name = "invited_at") private OffsetDateTime invitedAt;
    @Column(name = "started_at") private OffsetDateTime startedAt;
    @Column(name = "completed_at") private OffsetDateTime completedAt;
    @Column(name = "expires_at") private OffsetDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.INVITED;

    @Column(name = "overall_score") private Integer overallScore;
    @Column(name = "percentile") private Integer percentile;
    @Column(name = "recommendation", length = 30) private String recommendation;     // STRONG_HIRE / HIRE / MAYBE / NO_HIRE

    /** Section-wise scores — { logical: 78, verbal: 62, numerical: 80, ... }. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "section_scores", columnDefinition = "jsonb")
    private Map<String, Object> sectionScores;

    /** Vendor's raw JSON payload for auditing / dispute resolution. Redacted in audit log. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload", columnDefinition = "jsonb")
    private Map<String, Object> rawPayload;

    @Column(name = "report_uri", length = 1000) private String reportUri;

    public enum Status { INVITED, STARTED, COMPLETED, EXPIRED, CANCELLED, ERROR }
}
