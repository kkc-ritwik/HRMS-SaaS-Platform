package com.hrms.leave.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.events.annotation.PublishEvents;
import com.hrms.events.listener.EntityLifecyclePublisher;
import com.hrms.events.model.Topics;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "wfh_requests", indexes = {
        @Index(name = "ix_wfh_emp", columnList = "tenant_id,employee_id"),
        @Index(name = "ix_wfh_status", columnList = "tenant_id,status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@PublishEvents(topic = Topics.LEAVE, namespace = "leave.wfh")
@EntityListeners(EntityLifecyclePublisher.class)
public class WfhRequest extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(name = "duration_days", precision = 6, scale = 2)
    private java.math.BigDecimal durationDays;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private Status status = Status.PENDING;

    @Column(name = "approver_id")
    private UUID approverId;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "approver_comments", length = 500)
    private String approverComments;

    public enum Status { PENDING, APPROVED, REJECTED, CANCELLED }
}
