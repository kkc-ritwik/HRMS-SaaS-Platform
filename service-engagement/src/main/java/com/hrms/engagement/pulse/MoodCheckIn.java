package com.hrms.engagement.pulse;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Daily / weekly pulse check-in. Single-question eNPS-style score plus an optional comment.
 * Designed to be filled in &lt; 10 seconds — Zoho Pulse parity.
 *
 * Score scale: 1 (worst) → 5 (best). 4–5 = promoter, 3 = neutral, 1–2 = detractor.
 * Anonymous flag strips employeeId from manager-facing reports (only HR/admin can de-anonymise).
 */
@Entity
@Table(name = "engagement_mood_checkins", indexes = {
        @Index(name = "ix_mood_emp_date", columnList = "tenant_id,employee_id,check_in_date"),
        @Index(name = "ix_mood_tenant_date", columnList = "tenant_id,check_in_date")
})
@Auditable(value = "MoodCheckIn", redactFields = "comment")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MoodCheckIn extends BaseEntity {

    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "check_in_date", nullable = false) private LocalDate checkInDate;

    /** 1..5 — required. */
    @Column(name = "score", nullable = false) private Integer score;

    /** Free-text reason — optional, redacted in audit. */
    @Column(name = "comment", length = 2000) private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30)
    private Category category;

    @Column(name = "is_anonymous") private Boolean anonymous;

    /** Optional theme tag — workload / manager / team / growth / compensation / wellbeing. */
    @Column(name = "theme", length = 30) private String theme;

    public enum Category { DAILY, WEEKLY, POST_ONBOARDING, POST_PROMOTION, POST_REVIEW, EXIT }
}
