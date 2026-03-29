package com.hrms.performance.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Review extends BaseEntity {

    @Column(name = "cycle_id", nullable = false)
    private UUID cycleId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "manager_id")
    private UUID managerId;

    /** Same as employeeId for SELF; peerId for PEER; managerId for MANAGER. */
    @Column(name = "reviewer_id", nullable = false)
    private UUID reviewerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false, length = 20)
    private ReviewType reviewType = ReviewType.SELF;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReviewStatus status = ReviewStatus.DRAFT;

    @Column(name = "overall_rating", precision = 4, scale = 2)
    private BigDecimal overallRating;

    /** 1–5; Y-axis on the 9-box grid. */
    @Column(name = "potential_rating")
    private Integer potentialRating;

    /** Computed from goal + competency scores; X-axis on 9-box. */
    @Column(name = "performance_rating", precision = 4, scale = 2)
    private BigDecimal performanceRating;

    @Column(name = "goal_score", precision = 5, scale = 2)
    private BigDecimal goalScore;

    @Column(name = "competency_score", precision = 5, scale = 2)
    private BigDecimal competencyScore;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "development_areas", columnDefinition = "TEXT")
    private String developmentAreas;

    @Column(name = "manager_comments", columnDefinition = "TEXT")
    private String managerComments;

    @Column(name = "final_comments", columnDefinition = "TEXT")
    private String finalComments;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    // ── Enums ──────────────────────────────────────────────────────────────────

    public enum ReviewType { SELF, MANAGER, PEER, SKIP_LEVEL }

    public enum ReviewStatus { DRAFT, SUBMITTED, ACKNOWLEDGED, FINALIZED }
}
