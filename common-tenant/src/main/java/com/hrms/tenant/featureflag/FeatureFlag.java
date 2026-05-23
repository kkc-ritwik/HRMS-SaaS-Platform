package com.hrms.tenant.featureflag;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Tenant-scoped feature toggle. Acts like an in-house Unleash / LaunchDarkly:
 *   · key                   — stable identifier ("payroll.new-engine", "perf.calibration-v2")
 *   · enabled               — global default
 *   · enabledTenants        — explicit allow list (empty = use {@code enabled})
 *   · rolloutPercent        — 0-100; bucket employees by stable hash of employeeId
 *   · variantPayload        — optional JSON for multi-variant tests
 *
 * Reads are cached (TTL 60s) so checking a flag is < 1µs in steady state.
 */
@Entity
@Table(name = "feature_flags",
        uniqueConstraints = @UniqueConstraint(name = "uq_flag_key", columnNames = "key"),
        indexes = @Index(name = "ix_flag_key", columnList = "key"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FeatureFlag {

    @Id @GeneratedValue private UUID id;

    @Column(name = "key", length = 100, nullable = false) private String key;
    @Column(name = "description", length = 500) private String description;

    @Column(name = "enabled", nullable = false) private Boolean enabled = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "enabled_tenants", columnDefinition = "jsonb")
    private Map<String, Object> enabledTenants;

    @Column(name = "rollout_percent") private Integer rolloutPercent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "variant_payload", columnDefinition = "jsonb")
    private Map<String, Object> variantPayload;

    @Column(name = "created_at") private OffsetDateTime createdAt;
    @Column(name = "updated_at") private OffsetDateTime updatedAt;

    @PrePersist void prePersist() { createdAt = OffsetDateTime.now(); updatedAt = createdAt; }
    @PreUpdate  void preUpdate()  { updatedAt = OffsetDateTime.now(); }
}
