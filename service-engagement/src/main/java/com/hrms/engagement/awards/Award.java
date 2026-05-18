package com.hrms.engagement.awards;


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
import java.time.LocalDate;
import java.util.UUID;

/** Long-service awards, spot awards, peer-nominated awards, manager discretionary awards. */
@Entity
@Table(name = "engagement_awards",
        indexes = @Index(name = "ix_award_recipient", columnList = "tenant_id,recipient_employee_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@PublishEvents(topic = Topics.ENGAGEMENT, namespace = "engagement.award")
@EntityListeners(EntityLifecyclePublisher.class)
public class Award extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "award_type", length = 30, nullable = false)
    private AwardType awardType;

    @Column(name = "program_id") private UUID programId;
    @Column(name = "recipient_employee_id", nullable = false) private UUID recipientEmployeeId;
    @Column(name = "nominated_by_employee_id") private UUID nominatedByEmployeeId;
    @Column(name = "approved_by_employee_id") private UUID approvedByEmployeeId;

    @Column(name = "title", length = 200, nullable = false) private String title;
    @Column(name = "citation", length = 2000) private String citation;

    /** Monetary value (cash bonus, gift voucher amount). */
    @Column(name = "monetary_value", precision = 12, scale = 2) private BigDecimal monetaryValue;
    @Column(name = "currency", length = 3) private String currency;

    /** Points value for gamification leaderboards. */
    @Column(name = "points") private Integer points;

    @Column(name = "awarded_on") private LocalDate awardedOn;
    @Column(name = "certificate_uri", length = 1000) private String certificateUri;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.NOMINATED;

    public enum AwardType { LONG_SERVICE, SPOT, PEER_NOMINATED, MANAGER_DISCRETIONARY, ANNUAL, INNOVATION, CUSTOMER_HERO }
    public enum Status { NOMINATED, APPROVED, REJECTED, AWARDED, REDEEMED }
}
