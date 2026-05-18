package com.hrms.offboarding.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.events.annotation.PublishEvents;
import com.hrms.events.listener.EntityLifecyclePublisher;
import com.hrms.events.model.Topics;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "separations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Auditable("Separation")
@PublishEvents(topic = Topics.OFFBOARDING, namespace = "offboarding.separation")
@EntityListeners({AuditEntityListener.class, EntityLifecyclePublisher.class})
public class Separation extends BaseEntity {

    public enum SeparationType {
        RESIGNATION, TERMINATION, RETIREMENT, REDUNDANCY, CONTRACT_END
    }

    public enum SeparationStatus {
        INITIATED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "separation_type", nullable = false, length = 50)
    private SeparationType separationType;

    @Column(name = "last_working_date")
    private LocalDate lastWorkingDate;

    @Column(name = "notice_date")
    private LocalDate noticeDate;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SeparationStatus status;

    @Column(name = "initiated_by")
    private UUID initiatedBy;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "final_settlement_date")
    private LocalDate finalSettlementDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
