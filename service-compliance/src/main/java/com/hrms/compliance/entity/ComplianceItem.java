package com.hrms.compliance.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "compliance_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Auditable("ComplianceItem")
@EntityListeners(AuditEntityListener.class)
public class ComplianceItem extends BaseEntity {

    public enum ComplianceStatus {
        PENDING, IN_PROGRESS, COMPLIANT, NON_COMPLIANT, EXEMPT
    }

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "regulation", length = 200)
    private String regulation;

    @Column(name = "jurisdiction", length = 200)
    private String jurisdiction;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "recurring", nullable = false)
    private boolean recurring = false;

    @Column(name = "recurrence_period", length = 50)
    private String recurrencePeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ComplianceStatus status = ComplianceStatus.PENDING;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
