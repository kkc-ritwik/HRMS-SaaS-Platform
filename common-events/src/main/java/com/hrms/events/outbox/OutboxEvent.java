package com.hrms.events.outbox;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Outbox pattern â€” events written to the same DB transaction as the business state change,
 * then drained to Kafka by a background poller. Guarantees no event loss on commit.
 */
@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "ix_outbox_unsent", columnList = "sent_at,created_at"),
        @Index(name = "ix_outbox_topic", columnList = "topic")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OutboxEvent {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "topic", nullable = false, length = 100)
    private String topic;

    @Column(name = "event_id", length = 100, nullable = false, unique = true)
    private String eventId;

    @Column(name = "event_type", length = 100, nullable = false)
    private String eventType;

    @Column(name = "tenant_id", length = 100)
    private String tenantId;

    @Column(name = "aggregate_id", length = 100)
    private String aggregateId;

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "text")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @PrePersist
    void prePersist() { if (createdAt == null) createdAt = OffsetDateTime.now(); }
}
