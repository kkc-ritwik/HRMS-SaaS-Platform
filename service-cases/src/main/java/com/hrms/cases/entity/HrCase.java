package com.hrms.cases.entity;

import com.hrms.audit.annotation.Auditable;
import com.hrms.events.annotation.PublishEvents;
import com.hrms.events.listener.EntityLifecyclePublisher;
import com.hrms.events.model.Topics;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * HR cases — grievance, harassment (incl. POSH), discrimination, ethics, whistle-blower.
 * Distinct from helpdesk tickets because of confidentiality (only ICC / HR can view).
 */
@Entity
@Table(name = "hr_cases", indexes = {
    @Index(name = "ix_hrcase_tenant", columnList = "tenant_id"),
    @Index(name = "ix_hrcase_complainant", columnList = "complainant_employee_id"),
    @Index(name = "ix_hrcase_status", columnList = "status")
})
@Auditable(value = "HrCase", redactFields = "complainantEmployeeId,respondentEmployeeId,description")
@PublishEvents(topic = Topics.CASES, namespace = "cases")
@EntityListeners({com.hrms.audit.listener.AuditEntityListener.class, EntityLifecyclePublisher.class})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HrCase {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "case_number", length = 30, nullable = false, unique = true) private String caseNumber;
    @Enumerated(EnumType.STRING) @Column(length = 30) private CaseType type;
    @Column(length = 200) private String title;
    @Column(length = 5000) private String description;
    @Column(name = "complainant_employee_id") private UUID complainantEmployeeId;
    @Column(name = "respondent_employee_id") private UUID respondentEmployeeId;
    @Column(name = "is_anonymous") private Boolean isAnonymous;
    @Enumerated(EnumType.STRING) @Column(length = 30) private Severity severity;
    @Enumerated(EnumType.STRING) @Column(length = 30) private Status status;
    @Column(name = "assigned_to") private UUID assignedTo;
    @Column(name = "icc_committee_id") private UUID iccCommitteeId;
    @Column(name = "sla_due_date") private OffsetDateTime slaDueDate;
    @Column(name = "resolved_at") private OffsetDateTime resolvedAt;
    @Column(name = "resolution", length = 5000) private String resolution;
    @Column(name = "created_at") private OffsetDateTime createdAt;
    @Column(name = "updated_at") private OffsetDateTime updatedAt;

    @PrePersist void prePersist() {
        createdAt = OffsetDateTime.now(); updatedAt = createdAt;
        if (status == null) status = Status.NEW;
    }
    @PreUpdate void preUpdate() { updatedAt = OffsetDateTime.now(); }

    public enum CaseType { GRIEVANCE, HARASSMENT, POSH, DISCRIMINATION, ETHICS, WHISTLEBLOWER, OTHER }
    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }
    public enum Status { NEW, ACKNOWLEDGED, INVESTIGATING, AWAITING_INFO, ESCALATED, RESOLVED, CLOSED }
}
