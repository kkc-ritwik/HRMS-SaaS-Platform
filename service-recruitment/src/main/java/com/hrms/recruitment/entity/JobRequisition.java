package com.hrms.recruitment.entity;


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
@Table(name = "job_requisitions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("JobRequisition")
@PublishEvents(topic = Topics.RECRUITMENT, namespace = "recruitment.requisition")
@EntityListeners({AuditEntityListener.class, EntityLifecyclePublisher.class})
public class JobRequisition extends BaseEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "location", length = 200)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 30)
    private EmploymentType employmentType = EmploymentType.FULL_TIME;

    @Column(name = "positions_count", nullable = false)
    private int positionsCount = 1;

    @Column(name = "positions_filled", nullable = false)
    private int positionsFilled = 0;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "salary_min", precision = 14, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 14, scale = 2)
    private BigDecimal salaryMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RequisitionStatus status = RequisitionStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private Priority priority = Priority.MEDIUM;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "agency_id")
    private UUID agencyId;

    @Column(name = "requested_by", length = 100)
    private String requestedBy;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    // â”€â”€ Enums â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public enum EmploymentType { FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP }

    public enum RequisitionStatus {
        DRAFT, PENDING_APPROVAL, APPROVED, ACTIVE, ON_HOLD, CLOSED, CANCELLED
    }

    public enum Priority { LOW, MEDIUM, HIGH, URGENT }
}
