package com.hrms.recruitment.mobility;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Internal job board application — a current employee applies to an open requisition.
 * Kept separate from external {@code Application} so visibility rules and process flow
 * (manager notification, retention conversations, comp neutrality) can diverge.
 */
@Entity
@Table(name = "recruit_internal_applications", indexes = {
        @Index(name = "ix_iapp_emp", columnList = "tenant_id,employee_id"),
        @Index(name = "ix_iapp_req", columnList = "requisition_id")
})
@Auditable("InternalApplication")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InternalApplication extends BaseEntity {

    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "requisition_id", nullable = false) private UUID requisitionId;
    @Column(name = "current_manager_id") private UUID currentManagerId;

    /** Has the current manager been informed? Auto-set unless employee chose confidential mode. */
    @Column(name = "manager_notified") private Boolean managerNotified;
    @Column(name = "confidential_mode") private Boolean confidentialMode;

    @Column(name = "cover_note", length = 4000) private String coverNote;
    @Column(name = "resume_uri", length = 1000) private String resumeUri;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status = Status.SUBMITTED;

    @Column(name = "submitted_at") private OffsetDateTime submittedAt;
    @Column(name = "decision_at") private OffsetDateTime decisionAt;
    @Column(name = "decision_reason", length = 1000) private String decisionReason;

    public enum Status {
        SUBMITTED, MANAGER_REVIEW, SHORTLISTED, INTERVIEW, OFFER_EXTENDED,
        OFFERED_AND_ACCEPTED, OFFERED_AND_DECLINED, REJECTED, WITHDRAWN
    }
}
