package com.hrms.performance.career;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Individual Development Plan — owned by the employee, co-authored with the manager.
 * Tracks aspirations (next role/path), gaps, dev actions (training/mentorship/stretch),
 * and quarterly check-ins.
 */
@Entity
@Table(name = "perf_development_plans",
        indexes = @Index(name = "ix_devplan_emp", columnList = "tenant_id,employee_id"))
@Auditable("DevelopmentPlan")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DevelopmentPlan extends BaseEntity {

    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "manager_id") private UUID managerId;

    @Column(name = "target_role_designation_id") private UUID targetRoleDesignationId;
    @Column(name = "target_role_label", length = 200) private String targetRoleLabel;

    @Column(name = "horizon_months") private Integer horizonMonths;
    @Column(name = "review_cadence", length = 30) private String reviewCadence;     // QUARTERLY / SEMI_ANNUAL / ANNUAL

    /** Aspirations + motivations (employee's voice). */
    @Column(name = "aspirations", length = 4000) private String aspirations;

    /** Identified skill gaps (skill_id → currentLevel/targetLevel). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "skill_gaps", columnDefinition = "jsonb")
    private Map<String, Object> skillGaps;

    /** Dev actions — { type:TRAINING|MENTORSHIP|STRETCH_PROJECT|CERTIFICATION, title, dueDate, status, evidence } */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "actions", columnDefinition = "jsonb")
    private List<Map<String, Object>> actions;

    /** Quarterly check-in notes. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "check_ins", columnDefinition = "jsonb")
    private List<Map<String, Object>> checkIns;

    @Column(name = "start_date") private LocalDate startDate;
    @Column(name = "next_review_date") private LocalDate nextReviewDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.DRAFT;

    public enum Status { DRAFT, ACTIVE, ON_HOLD, COMPLETED, CANCELLED }
}
