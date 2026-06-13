package com.hrms.engagement.award;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Structured recognition award — a nomination that goes through approval before
 * being granted (Spot Award, Employee of the Month, Long Service, etc.).
 * Complements peer {@code Kudos} (which is instant and unmoderated).
 */
@Entity
@Table(name = "awards", indexes = {
        @Index(name = "ix_award_nominee", columnList = "tenant_id,nominee_id"),
        @Index(name = "ix_award_status", columnList = "tenant_id,status")
})
@Auditable("Award")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Award {

    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;

    @Column(name = "title", length = 200, nullable = false) private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "award_type", length = 40, nullable = false)
    private AwardType awardType;

    @Column(name = "nominee_id", nullable = false) private UUID nomineeId;
    @Column(name = "nominated_by") private UUID nominatedBy;
    @Column(name = "reason", length = 2000) private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private Status status = Status.NOMINATED;

    @Column(name = "period", length = 40) private String period;
    @Column(name = "points") private Integer points;
    @Column(name = "monetary_value", precision = 12, scale = 2) private BigDecimal monetaryValue;
    @Column(name = "currency", length = 3) private String currency;

    @Column(name = "decided_by") private UUID decidedBy;
    @Column(name = "decided_at") private OffsetDateTime decidedAt;
    @Column(name = "decision_notes", length = 1000) private String decisionNotes;

    @Column(name = "created_at") private OffsetDateTime createdAt;
    @Column(name = "updated_at") private OffsetDateTime updatedAt;

    @PrePersist void prePersist() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
        if (status == null) status = Status.NOMINATED;
    }

    @PreUpdate void preUpdate() { updatedAt = OffsetDateTime.now(); }

    public enum AwardType {
        SPOT_AWARD, EMPLOYEE_OF_THE_MONTH, EMPLOYEE_OF_THE_YEAR, LONG_SERVICE,
        TEAM_AWARD, INNOVATION, LEADERSHIP, CUSTOMER_HERO, VALUES_CHAMPION, OTHER
    }

    public enum Status { NOMINATED, APPROVED, REJECTED, AWARDED }
}
