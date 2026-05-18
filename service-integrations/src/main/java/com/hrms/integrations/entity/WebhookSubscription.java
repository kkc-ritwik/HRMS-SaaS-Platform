package com.hrms.integrations.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "webhook_subscriptions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Auditable("WebhookSubscription")
@EntityListeners(AuditEntityListener.class)
public class WebhookSubscription {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(nullable = false, length = 200) private String name;
    @Column(name = "target_url", nullable = false, length = 1000) private String targetUrl;
    /** HMAC signing secret â€” included in X-Hrms-Signature header on each delivery. */
    @Column(length = 200) private String secret;
    /** List of subscribed event types, e.g. ["employee.created","leave.approved"] or ["*"]. */
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "event_types", columnDefinition = "jsonb")
    private List<String> eventTypes;
    @Column(name = "is_active") private Boolean isActive;
    @Column(name = "retry_max", nullable = false) private Integer retryMax;
    @Column(name = "timeout_ms", nullable = false) private Integer timeoutMs;
    @Column(name = "created_at") private OffsetDateTime createdAt;
    @PrePersist void prePersist() {
        createdAt = OffsetDateTime.now();
        if (retryMax == null) retryMax = 5;
        if (timeoutMs == null) timeoutMs = 5000;
        if (isActive == null) isActive = true;
    }
}
