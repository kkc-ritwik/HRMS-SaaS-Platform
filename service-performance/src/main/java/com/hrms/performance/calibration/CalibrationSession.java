package com.hrms.performance.calibration;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A calibration session where managers + HR review proposed ratings as a group
 * to enforce consistency / bell-curve before reviews are finalized.
 */
@Entity
@Table(name = "calibration_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("CalibrationSession")
@EntityListeners(AuditEntityListener.class)
public class CalibrationSession extends BaseEntity {

    @Column(name = "cycle_id", nullable = false)
    private UUID cycleId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "facilitator_id", nullable = false)
    private UUID facilitatorId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "participant_ids", columnDefinition = "jsonb")
    private List<UUID> participantIds;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status = Status.SCHEDULED;

    /** Target distribution as { "5": 10, "4": 25, "3": 50, "2": 10, "1": 5 } percentages. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_distribution", columnDefinition = "jsonb")
    private java.util.Map<String, Integer> targetDistribution;

    @Column(name = "notes", length = 4000)
    private String notes;

    public enum Status { SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED }
}
