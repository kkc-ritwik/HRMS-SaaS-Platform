package com.hrms.workflow.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "workflow_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Auditable("WorkflowStep")
@EntityListeners(AuditEntityListener.class)
public class WorkflowStep extends BaseEntity {

    public enum StepType {
        APPROVAL, NOTIFICATION, ACTION, CONDITION
    }

    public enum ApproverType {
        ROLE, EMPLOYEE, MANAGER, HR
    }

    @Column(name = "workflow_id", nullable = false)
    private UUID workflowId;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(name = "step_name", nullable = false, length = 200)
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false, length = 30)
    private StepType stepType;

    @Enumerated(EnumType.STRING)
    @Column(name = "approver_type", length = 30)
    private ApproverType approverType;

    @Column(name = "approver_id")
    private UUID approverId;

    @Column(name = "approver_role", length = 100)
    private String approverRole;

    @Column(name = "can_delegate", nullable = false)
    private boolean canDelegate = false;

    @Column(name = "sla_hours", nullable = false)
    private int slaHours = 24;

    @Column(name = "escalation_to")
    private UUID escalationTo;

    @Column(name = "required", nullable = false)
    private boolean required = true;

    /**
     * Steps with the same parallelGroup execute concurrently â€” the group's children must ALL
     * resolve (per quorum policy) before the workflow advances to the next stepOrder.
     * Null = sequential.
     */
    @Column(name = "parallel_group", length = 50)
    private String parallelGroup;

    /** ANY = first response decides; ALL = wait for every approver; MAJORITY = quorum. */
    @Enumerated(EnumType.STRING)
    @Column(name = "quorum_policy", length = 30)
    private QuorumPolicy quorumPolicy;

    /** For MAJORITY â€” minimum % of approvers needed (e.g. 60 = 60%). Default 51. */
    @Column(name = "quorum_threshold_percent")
    private Integer quorumThresholdPercent;

    public enum QuorumPolicy { ANY, ALL, MAJORITY }
}
