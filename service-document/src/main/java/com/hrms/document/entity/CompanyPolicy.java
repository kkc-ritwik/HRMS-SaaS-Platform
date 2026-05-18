package com.hrms.document.entity;


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

@Entity
@Table(name = "company_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Auditable("CompanyPolicy")
@PublishEvents(topic = Topics.DOCUMENT, namespace = "document.policy")
@EntityListeners({AuditEntityListener.class, EntityLifecyclePublisher.class})
public class CompanyPolicy extends BaseEntity {

    public enum PolicyStatus {
        DRAFT, ACTIVE, ARCHIVED
    }

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "policy_type", length = 100)
    private String policyType;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PolicyStatus status = PolicyStatus.DRAFT;

    @Column(name = "requires_acknowledgement", nullable = false)
    private boolean requiresAcknowledgement;
}
