package com.hrms.performance.continuous;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Lightweight in-the-moment feedback â€” fires outside any review cycle.
 * Use cases: "great job on the demo", "consider tightening the standup", "you led that
 * customer call really well". Manager / peer / self / skip-level visibility configurable.
 */
@Entity
@Table(name = "perf_continuous_feedback",
        indexes = {
            @Index(name = "ix_cfb_recipient", columnList = "tenant_id,recipient_employee_id"),
            @Index(name = "ix_cfb_giver", columnList = "tenant_id,giver_employee_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ContinuousFeedback extends BaseEntity {

    @Column(name = "giver_employee_id", nullable = false) private UUID giverEmployeeId;
    @Column(name = "recipient_employee_id", nullable = false) private UUID recipientEmployeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", length = 30, nullable = false)
    private Type feedbackType;

    @Column(name = "content", length = 2000, nullable = false) private String content;

    /** Optional reference to a goal / OKR / project this relates to. */
    @Column(name = "reference_type", length = 50) private String referenceType;
    @Column(name = "reference_id") private UUID referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", length = 20, nullable = false)
    private Visibility visibility = Visibility.RECIPIENT_AND_MANAGER;

    @Column(name = "anonymous") private Boolean anonymous;
    @Column(name = "acknowledged") private Boolean acknowledged;

    public enum Type { PRAISE, CONSTRUCTIVE, COACHING, REQUEST_FOR_FEEDBACK }
    public enum Visibility { PRIVATE, RECIPIENT_AND_MANAGER, PUBLIC }
}
