package com.hrms.compensation.increment;


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
import java.util.UUID;

@Entity
@Table(name = "increment_proposals",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cycle_id","employee_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@PublishEvents(topic = Topics.PAYROLL, namespace = "compensation.increment")
@EntityListeners(EntityLifecyclePublisher.class)
public class IncrementProposal extends BaseEntity {

    @Column(name = "cycle_id", nullable = false) private UUID cycleId;
    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "manager_id") private UUID managerId;

    @Column(name = "current_base", precision = 14, scale = 2) private BigDecimal currentBase;
    @Column(name = "proposed_base", precision = 14, scale = 2) private BigDecimal proposedBase;
    @Column(name = "hike_percent", precision = 6, scale = 2) private BigDecimal hikePercent;

    @Column(name = "bonus_amount", precision = 14, scale = 2) private BigDecimal bonusAmount;
    @Column(name = "equity_units") private Integer equityUnits;

    @Column(name = "rating", precision = 4, scale = 2) private BigDecimal rating;
    @Column(name = "compa_ratio_before", precision = 5, scale = 4) private BigDecimal compaRatioBefore;
    @Column(name = "compa_ratio_after", precision = 5, scale = 4) private BigDecimal compaRatioAfter;

    @Column(name = "justification", length = 2000) private String justification;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status = Status.PROPOSED;

    @Column(name = "approved_at") private Instant approvedAt;
    @Column(name = "approved_by") private UUID approvedBy;
    @Column(name = "letter_storage_uri", length = 1000) private String letterStorageUri;

    public enum Status { PROPOSED, REVISED, APPROVED, REJECTED, PUBLISHED }
}
