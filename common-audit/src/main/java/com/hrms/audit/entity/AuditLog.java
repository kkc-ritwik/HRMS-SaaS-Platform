package com.hrms.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "ix_audit_tenant_entity", columnList = "tenant_id,entity_name,entity_id"),
        @Index(name = "ix_audit_actor", columnList = "actor_id"),
        @Index(name = "ix_audit_created_at", columnList = "created_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "tenant_id", length = 100)
    private String tenantId;

    @Column(name = "entity_name", length = 100, nullable = false)
    private String entityName;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 20, nullable = false)
    private AuditAction action;

    @Column(name = "actor_id", length = 100)
    private String actorId;

    @Column(name = "actor_email", length = 255)
    private String actorEmail;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before_value", columnDefinition = "jsonb")
    private Map<String, Object> beforeValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after_value", columnDefinition = "jsonb")
    private Map<String, Object> afterValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "changed_fields", columnDefinition = "jsonb")
    private Map<String, Object> changedFields;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    /** SHA-256 of previous audit row's currentHash — null only for the genesis row. */
    @Column(name = "prev_hash", length = 64)
    private String prevHash;

    /** SHA-256 of (prevHash || canonical(this row without hash)) — tamper-evidence. */
    @Column(name = "current_hash", length = 64)
    private String currentHash;

    @PrePersist
    void prePersist() { if (createdAt == null) createdAt = OffsetDateTime.now(); }
}
