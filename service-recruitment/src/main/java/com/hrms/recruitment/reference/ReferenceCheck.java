package com.hrms.recruitment.reference;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * One referee per candidate. Captures contact, relationship, and structured feedback
 * once the referee responds (either by phone-script entered by recruiter or via the
 * referee-facing form whose token is emailed to them).
 */
@Entity
@Table(name = "reference_checks",
        indexes = @Index(name = "ix_refcheck_candidate", columnList = "tenant_id,candidate_id"))
@Auditable(value = "ReferenceCheck", redactFields = "refereePhone,refereeEmail,structuredFeedback")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReferenceCheck extends BaseEntity {

    @Column(name = "candidate_id", nullable = false) private UUID candidateId;

    @Column(name = "referee_name", length = 200, nullable = false) private String refereeName;
    @Column(name = "referee_email", length = 200) private String refereeEmail;
    @Column(name = "referee_phone", length = 30) private String refereePhone;
    @Column(name = "referee_company", length = 200) private String refereeCompany;
    @Column(name = "referee_title", length = 200) private String refereeTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship", length = 30)
    private Relationship relationship;

    @Column(name = "request_token", length = 100, unique = true) private String requestToken;
    @Column(name = "requested_at") private Instant requestedAt;
    @Column(name = "responded_at") private Instant respondedAt;

    /** Map of question-id → answer; mirrors the question template at request time. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "structured_feedback", columnDefinition = "jsonb")
    private Map<String, Object> structuredFeedback;

    @Column(name = "free_text_feedback", length = 5000) private String freeTextFeedback;

    @Column(name = "would_rehire") private Boolean wouldRehire;
    @Column(name = "overall_rating") private Integer overallRating;       // 1-5

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status = Status.PENDING;

    public enum Relationship { DIRECT_MANAGER, SKIP_LEVEL, PEER, CLIENT, PROFESSOR, MENTOR, OTHER }
    public enum Status { PENDING, INVITED, RESPONDED, EXPIRED, DECLINED }
}
