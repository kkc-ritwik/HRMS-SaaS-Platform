package com.hrms.engagement.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Public peer recognition / praise. */
@Entity
@Table(name = "kudos", indexes = {
        @Index(name = "ix_kudos_recipient", columnList = "tenant_id,recipient_id"),
        @Index(name = "ix_kudos_giver", columnList = "tenant_id,giver_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Kudos {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "giver_id", nullable = false) private UUID giverId;
    @Column(name = "recipient_id", nullable = false) private UUID recipientId;
    @Column(length = 100) private String value;        // e.g. "Customer Obsession", "Ownership"
    @Column(length = 1000, nullable = false) private String message;
    @Column(name = "is_public") private Boolean isPublic;
    @Column(name = "points") private Integer points;
    @Column(name = "created_at") private OffsetDateTime createdAt;
    @PrePersist void prePersist() { createdAt = OffsetDateTime.now(); }
}
