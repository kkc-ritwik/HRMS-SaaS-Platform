package com.hrms.engagement.stay;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Stay interview — proactive retention conversation conducted by manager / HRBP with a
 * currently-employed high-potential or flight-risk employee. Opposite of an exit interview:
 * the goal is to understand what makes them stay, what would make them leave, and intervene
 * before they resign.
 *
 * Cadence (defaults): 30 days post-hire, 90 days post-hire, annually thereafter, and
 * ad-hoc when flight-risk indicators trigger (e.g. low pulse score, missed promotion).
 */
@Entity
@Table(name = "engagement_stay_interviews", indexes = {
        @Index(name = "ix_stay_employee", columnList = "tenant_id,employee_id"),
        @Index(name = "ix_stay_status", columnList = "tenant_id,status,scheduled_date")
})
@Auditable(value = "StayInterview", redactFields = "notes,responses")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class StayInterview extends BaseEntity {

    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "interviewer_id", nullable = false) private UUID interviewerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger", length = 30, nullable = false)
    private Trigger trigger;

    @Column(name = "scheduled_date") private LocalDate scheduledDate;
    @Column(name = "conducted_at") private OffsetDateTime conductedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.SCHEDULED;

    /** 1..5 — overall engagement / commitment vibe. */
    @Column(name = "engagement_score") private Integer engagementScore;

    /** Manager's estimate of flight risk. */
    @Enumerated(EnumType.STRING)
    @Column(name = "flight_risk", length = 10)
    private FlightRisk flightRisk;

    /** Responses keyed by question id from the StayQuestionBank — { questionId: answer }. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "responses", columnDefinition = "jsonb")
    private Map<String, Object> responses;

    @Column(name = "notes", length = 5000) private String notes;

    /** Action items committed by manager (training, role change, comp review). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_items", columnDefinition = "jsonb")
    private List<Map<String, Object>> actionItems;

    @Column(name = "next_review_date") private LocalDate nextReviewDate;

    public enum Trigger { THIRTY_DAY, NINETY_DAY, ANNUAL, AD_HOC, FLIGHT_RISK, POST_PROMOTION, POST_REORG }
    public enum Status { SCHEDULED, COMPLETED, SKIPPED, EMPLOYEE_DECLINED }
    public enum FlightRisk { LOW, MEDIUM, HIGH, CRITICAL }
}
