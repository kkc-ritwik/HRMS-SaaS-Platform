package com.hrms.timesheet.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.events.annotation.PublishEvents;
import com.hrms.events.listener.EntityLifecyclePublisher;
import com.hrms.events.model.Topics;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "weekly_timesheets",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","employee_id","week_start"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@PublishEvents(topic = Topics.TIMESHEET, namespace = "timesheet.week")
@EntityListeners(EntityLifecyclePublisher.class)
public class WeeklyTimesheet {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "week_start", nullable = false) private LocalDate weekStart;
    @Column(name = "total_hours", precision = 6, scale = 2) private BigDecimal totalHours;
    @Column(name = "billable_hours", precision = 6, scale = 2) private BigDecimal billableHours;
    @Enumerated(EnumType.STRING) @Column(length = 30) private Status status;
    @Column(name = "submitted_at") private OffsetDateTime submittedAt;
    @Column(name = "approver_id") private UUID approverId;
    @Column(name = "approved_at") private OffsetDateTime approvedAt;

    public enum Status { DRAFT, SUBMITTED, APPROVED, REJECTED }
}
