package com.hrms.recruitment.scorecard;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * One scorecard submission per panellist per interview.
 *
 *   competencyScores: [{ id, score, comment }] — matches template's competency ids
 *   strengths/areasOfConcern: free-text
 *   recommendation: STRONG_HIRE / HIRE / NO_HIRE / STRONG_NO_HIRE
 *
 * Locked once submitted to prevent post-hoc influence from other panellists.
 */
@Entity
@Table(name = "recruit_interview_scorecards", indexes = {
        @Index(name = "ix_score_interview", columnList = "interview_id"),
        @Index(name = "ix_score_application", columnList = "tenant_id,application_id")
})
@Auditable(value = "InterviewScorecard", redactFields = "privateNotes")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InterviewScorecard extends BaseEntity {

    @Column(name = "interview_id", nullable = false) private UUID interviewId;
    @Column(name = "application_id", nullable = false) private UUID applicationId;
    @Column(name = "candidate_id", nullable = false) private UUID candidateId;
    @Column(name = "panellist_id", nullable = false) private UUID panellistId;
    @Column(name = "template_id") private UUID templateId;

    /** [{ id, score, comment }] */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "competency_scores", columnDefinition = "jsonb")
    private List<Map<String, Object>> competencyScores;

    @Column(name = "overall_score") private Integer overallScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation", length = 20)
    private Recommendation recommendation;

    @Column(name = "strengths", length = 4000) private String strengths;
    @Column(name = "areas_of_concern", length = 4000) private String areasOfConcern;
    @Column(name = "would_work_with") private Boolean wouldWorkWith;
    @Column(name = "private_notes", length = 4000) private String privateNotes;
    @Column(name = "submitted_at") private OffsetDateTime submittedAt;
    @Column(name = "locked") private Boolean locked = false;

    public enum Recommendation { STRONG_HIRE, HIRE, MIXED, NO_HIRE, STRONG_NO_HIRE }
}
