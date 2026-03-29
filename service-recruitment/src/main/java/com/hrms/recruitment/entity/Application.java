package com.hrms.recruitment.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "applications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Application extends BaseEntity {

    @Column(name = "requisition_id", nullable = false)
    private UUID requisitionId;

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false, length = 20)
    private ApplicationStage stage = ApplicationStage.APPLIED;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt = Instant.now();

    @Column(name = "stage_changed_at", nullable = false)
    private Instant stageChangedAt = Instant.now();

    @Column(name = "current_ctc", precision = 14, scale = 2)
    private BigDecimal currentCtc;

    @Column(name = "expected_ctc", precision = 14, scale = 2)
    private BigDecimal expectedCtc;

    @Column(name = "notice_period_days")
    private Integer noticePeriodDays;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ── Enum ──────────────────────────────────────────────────────────────────

    public enum ApplicationStage {
        APPLIED, SCREENING, PHONE_SCREEN, TECHNICAL, HR, OFFER, HIRED, REJECTED, WITHDRAWN
    }
}
