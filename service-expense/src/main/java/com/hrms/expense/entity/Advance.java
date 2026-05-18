package com.hrms.expense.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.events.annotation.PublishEvents;
import com.hrms.events.listener.EntityLifecyclePublisher;
import com.hrms.events.model.Topics;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "advances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Auditable("Advance")
@PublishEvents(topic = Topics.EXPENSE, namespace = "expense.advance")
@EntityListeners({AuditEntityListener.class, EntityLifecyclePublisher.class})
public class Advance extends BaseEntity {

    public enum AdvanceStatus {
        REQUESTED, APPROVED, DISBURSED, SETTLED, REJECTED
    }

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "USD";

    @Column(name = "purpose", columnDefinition = "TEXT")
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AdvanceStatus status = AdvanceStatus.REQUESTED;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt = Instant.now();

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "disbursed_at")
    private Instant disbursedAt;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "settled_amount", precision = 15, scale = 2)
    private BigDecimal settledAmount;
}
