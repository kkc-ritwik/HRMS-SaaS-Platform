package com.hrms.integrations.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "webhook_deliveries", indexes = {
        @Index(name = "ix_delivery_sub", columnList = "subscription_id"),
        @Index(name = "ix_delivery_status", columnList = "status,next_attempt_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WebhookDelivery {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "subscription_id", nullable = false) private UUID subscriptionId;
    @Column(name = "event_id", length = 100, nullable = false) private String eventId;
    @Column(name = "event_type", length = 100, nullable = false) private String eventType;
    @Lob @Column(name = "payload", nullable = false, columnDefinition = "text") private String payload;
    @Enumerated(EnumType.STRING) @Column(length = 30) private Status status;
    @Column(name = "attempts", nullable = false) private Integer attempts;
    @Column(name = "next_attempt_at") private OffsetDateTime nextAttemptAt;
    @Column(name = "last_response_code") private Integer lastResponseCode;
    @Column(name = "last_error", length = 2000) private String lastError;
    @Column(name = "created_at") private OffsetDateTime createdAt;
    @Column(name = "delivered_at") private OffsetDateTime deliveredAt;

    @PrePersist void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (attempts == null) attempts = 0;
        if (status == null) status = Status.PENDING;
        if (nextAttemptAt == null) nextAttemptAt = createdAt;
    }
    public enum Status { PENDING, DELIVERED, FAILED, ABANDONED }
}
