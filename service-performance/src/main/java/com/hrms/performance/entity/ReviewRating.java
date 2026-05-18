package com.hrms.performance.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "review_ratings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("ReviewRating")
@EntityListeners(AuditEntityListener.class)
public class ReviewRating extends BaseEntity {

    @Column(name = "review_id", nullable = false)
    private UUID reviewId;

    /** Nullable â€” set for COMPETENCY type ratings. */
    @Column(name = "competency_id")
    private UUID competencyId;

    /** Nullable â€” set for GOAL type ratings. */
    @Column(name = "goal_id")
    private UUID goalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rating_type", nullable = false, length = 20)
    private RatingType ratingType;

    @Column(name = "rating", nullable = false, precision = 4, scale = 2)
    private BigDecimal rating;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    // â”€â”€ Enum â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public enum RatingType { COMPETENCY, GOAL, OVERALL }
}
