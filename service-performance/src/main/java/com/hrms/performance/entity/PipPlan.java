package com.hrms.performance.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pip_plans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PipPlan extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "manager_id", nullable = false)
    private UUID managerId;

    @Column(name = "hr_manager_id")
    private UUID hrManagerId;

    @Column(name = "review_cycle_id")
    private UUID reviewCycleId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PipStatus status = PipStatus.DRAFT;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "improvement_areas", columnDefinition = "jsonb")
    private List<ImprovementArea> improvementAreas;

    @Column(name = "support_provided", columnDefinition = "TEXT")
    private String supportProvided;

    @Enumerated(EnumType.STRING)
    @Column(name = "check_in_frequency", nullable = false, length = 20)
    private CheckInFrequency checkInFrequency = CheckInFrequency.WEEKLY;

    @Column(name = "outcome_notes", columnDefinition = "TEXT")
    private String outcomeNotes;

    @Column(name = "closed_at")
    private Instant closedAt;

    // ── Enums ──────────────────────────────────────────────────────────────────

    public enum PipStatus { DRAFT, ACTIVE, COMPLETED, FAILED, WITHDRAWN }

    public enum CheckInFrequency { WEEKLY, BIWEEKLY, MONTHLY }
}
