package com.hrms.engagement.wellness;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Wellness initiative — step-count challenge, meditation, gym reimbursement, health-checkup
 * drive. Employees join (Participation), record activity (ActivityLog), and earn points
 * mapped to the rewards catalog.
 */
@Entity
@Table(name = "engagement_wellness_programs",
        indexes = @Index(name = "ix_well_active", columnList = "tenant_id,active"))
@Auditable("WellnessProgram")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class WellnessProgram extends BaseEntity {

    @Column(name = "code", length = 50, nullable = false) private String code;
    @Column(name = "name", length = 200, nullable = false) private String name;
    @Column(name = "description", length = 2000) private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30, nullable = false)
    private Category category;

    @Column(name = "start_date") private LocalDate startDate;
    @Column(name = "end_date") private LocalDate endDate;

    @Column(name = "goal_metric", length = 50) private String goalMetric;     // STEPS / MINUTES / SESSIONS / WORKOUTS
    @Column(name = "goal_target") private Long goalTarget;
    @Column(name = "points_per_unit") private Integer pointsPerUnit;
    @Column(name = "max_points_per_day") private Integer maxPointsPerDay;

    @Column(name = "image_url", length = 500) private String imageUrl;
    @Column(name = "active") private Boolean active = true;

    public enum Category { STEP_CHALLENGE, MEDITATION, NUTRITION, FITNESS, MENTAL_HEALTH, HEALTH_CHECKUP, GYM, EAP, OTHER }
}
