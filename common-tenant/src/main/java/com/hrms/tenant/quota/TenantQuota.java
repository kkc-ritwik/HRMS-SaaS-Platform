package com.hrms.tenant.quota;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Soft + hard quotas per tenant. Service-side gates check current usage against {@code limit}
 * before allowing the action; usage counters are bumped on success.
 *
 * Quota keys (canonical):
 *   max_employees, max_storage_bytes, max_api_requests_per_day,
 *   max_payroll_runs_per_month, max_concurrent_sessions, max_kafka_events_per_hour,
 *   max_tenants_per_org, max_users_per_role, max_open_requisitions,
 *   max_documents_per_employee
 *
 * Status:
 *   ACTIVE         — usage < softLimit, all good.
 *   WARNING        — softLimit ≤ usage < hardLimit, banner shown to admins.
 *   THROTTLED      — usage ≥ hardLimit, writes return 429.
 *   SUSPENDED      — tenant frozen by ops (billing / TOS violation).
 */
@Entity
@Table(name = "tenant_quotas",
        uniqueConstraints = @UniqueConstraint(name = "uq_quota_key", columnNames = {"tenant_id","quota_key"}),
        indexes = @Index(name = "ix_quota_tenant", columnList = "tenant_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TenantQuota {

    @Id @GeneratedValue private UUID id;

    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "quota_key", length = 100, nullable = false) private String quotaKey;

    @Column(name = "soft_limit") private Long softLimit;
    @Column(name = "hard_limit") private Long hardLimit;
    @Column(name = "current_usage") private Long currentUsage = 0L;

    @Column(name = "reset_at") private OffsetDateTime resetAt;          // for time-windowed quotas
    @Column(name = "reset_interval", length = 30) private String resetInterval;  // HOUR / DAY / MONTH

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "updated_at") private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version") private Long version;

    @PreUpdate @PrePersist void touch() { updatedAt = OffsetDateTime.now(); }

    public enum Status { ACTIVE, WARNING, THROTTLED, SUSPENDED }
}
