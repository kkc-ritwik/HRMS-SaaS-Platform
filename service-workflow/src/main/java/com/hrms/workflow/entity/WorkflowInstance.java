package com.hrms.workflow.entity;


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

@Entity
@Table(name = "workflow_instances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Auditable("WorkflowInstance")
@EntityListeners(AuditEntityListener.class)
public class WorkflowInstance extends BaseEntity {

    public enum InstanceStatus {
        PENDING, IN_PROGRESS, APPROVED, REJECTED, CANCELLED, ESCALATED
    }

    @Column(name = "workflow_id", nullable = false)
    private UUID workflowId;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "initiated_by", nullable = false)
    private UUID initiatedBy;

    @Column(name = "current_step_order", nullable = false)
    private int currentStepOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private InstanceStatus status = InstanceStatus.PENDING;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context_data", columnDefinition = "jsonb")
    private List<String> contextData;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /** SLA deadline for the current step. Bumped on each transition by WorkflowStep.timeoutDays. */
    @Column(name = "sla_due_at")
    private Instant slaDueAt;

    @Column(name = "escalated_at")
    private Instant escalatedAt;
}
