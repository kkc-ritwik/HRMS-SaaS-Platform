package com.hrms.performance.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "review_cycles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("ReviewCycle")
@EntityListeners(AuditEntityListener.class)
public class ReviewCycle extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "cycle_type", nullable = false, length = 20)
    private CycleType cycleType = CycleType.ANNUAL;

    @Column(name = "fiscal_year", length = 10)
    private String fiscalYear;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CycleStatus status = CycleStatus.DRAFT;

    @Column(name = "self_review_deadline")
    private LocalDate selfReviewDeadline;

    @Column(name = "manager_review_deadline")
    private LocalDate managerReviewDeadline;

    @Column(name = "calibration_date")
    private LocalDate calibrationDate;

    @Column(name = "include_goal_rating", nullable = false)
    private boolean includeGoalRating = true;

    @Column(name = "include_competency_rating", nullable = false)
    private boolean includeCompetencyRating = true;

    @Column(name = "include_360_feedback", nullable = false)
    private boolean include360Feedback = false;

    @Column(name = "rating_scale", nullable = false)
    private int ratingScale = 5;

    // â”€â”€ Enums â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public enum CycleType { ANNUAL, HALF_YEARLY, QUARTERLY, MONTHLY, PROBATION }

    public enum CycleStatus {
        DRAFT, ACTIVE, SELF_REVIEW, MANAGER_REVIEW, CALIBRATION, FINALIZED, CLOSED
    }
}
