package com.hrms.onboarding.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "probation_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProbationReview extends BaseEntity {

    public enum ReviewStatus {
        SCHEDULED, COMPLETED, EXTENDED, PASSED, FAILED
    }

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "reviewer_id")
    private UUID reviewerId;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @Column(name = "period_months", nullable = false)
    @Builder.Default
    private int periodMonths = 3;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.SCHEDULED;

    @Column(name = "overall_rating", length = 50)
    private String overallRating;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "extended_until")
    private LocalDate extendedUntil;
}
