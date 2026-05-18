package com.hrms.auth.apikey;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Per-tenant API key. The plaintext key is shown only once at issue time; we store
 * SHA-256 + a short prefix to support partial display ("hrms_pk_a1b2â€¦") in the UI.
 * Scopes constrain what the key can call (read:employee, write:leave, etc.).
 */
@Entity
@Table(name = "api_keys",
        indexes = @Index(name = "ix_apikey_hash", columnList = "key_hash", unique = true))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ApiKey extends BaseEntity {

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    /** First 8 chars of the key â€” safe to display. */
    @Column(name = "key_prefix", length = 16, nullable = false)
    private String keyPrefix;

    /** SHA-256 of the full key. */
    @Column(name = "key_hash", length = 64, nullable = false, unique = true)
    private String keyHash;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scopes", columnDefinition = "jsonb")
    private Set<String> scopes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allowed_ips", columnDefinition = "jsonb")
    private Set<String> allowedIps;

    @Column(name = "expires_at") private Instant expiresAt;
    @Column(name = "last_used_at") private Instant lastUsedAt;
    @Column(name = "use_count", nullable = false) private long useCount = 0;
    @Column(name = "revoked_at") private Instant revokedAt;
    @Column(name = "is_active", nullable = false) private boolean active = true;
}
