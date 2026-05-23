package com.hrms.engagement.wellness;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * One activity log row per employee per program per day. Source tells you whether the
 * number came from a wearable (Fitbit, Apple Watch, Google Fit) or manual entry.
 */
@Entity
@Table(name = "engagement_wellness_logs",
        uniqueConstraints = @UniqueConstraint(name = "uq_wellness_log_day",
                columnNames = {"tenant_id", "employee_id", "program_id", "log_date"}),
        indexes = {
                @Index(name = "ix_wlog_emp_date", columnList = "tenant_id,employee_id,log_date"),
                @Index(name = "ix_wlog_program", columnList = "program_id,log_date")
        })
@Auditable("WellnessActivityLog")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class WellnessActivityLog extends BaseEntity {

    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "program_id", nullable = false) private UUID programId;
    @Column(name = "log_date", nullable = false) private LocalDate logDate;

    @Column(name = "metric_value", nullable = false) private Long metricValue;   // steps, minutes, sessions
    @Column(name = "points_earned") private Integer pointsEarned;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 20, nullable = false)
    private Source source = Source.MANUAL;

    @Column(name = "device_id", length = 100) private String deviceId;
    @Column(name = "notes", length = 500) private String notes;

    public enum Source { MANUAL, FITBIT, APPLE_HEALTH, GOOGLE_FIT, GARMIN, WEBHOOK, OTHER }
}
