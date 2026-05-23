package com.hrms.tenant.async;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Long-running async operation record. Used by bulk imports, payroll runs, report exports,
 * GDPR data extracts — anywhere the HTTP request can't block until completion.
 *
 * Workflow:
 *   POST returns 202 Accepted with {operationId, statusUrl, location}.
 *   Client polls GET /api/operations/{id} until status == COMPLETED or FAILED.
 *   COMPLETED operations include resultLocation pointing to the artefact (S3 URI).
 *
 * Stored in the originating service's database; cleaned up after retentionUntil.
 */
@Entity
@Table(name = "async_operations", indexes = {
        @Index(name = "ix_op_tenant_status", columnList = "tenant_id,status"),
        @Index(name = "ix_op_actor", columnList = "tenant_id,actor_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AsyncOperation {

    @Id @GeneratedValue private UUID id;

    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "actor_id", length = 100) private String actorId;

    @Column(name = "operation_type", length = 80, nullable = false) private String operationType;
    @Column(name = "description", length = 500) private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.QUEUED;

    @Column(name = "progress_percent") private Integer progressPercent = 0;
    @Column(name = "items_total") private Long itemsTotal;
    @Column(name = "items_processed") private Long itemsProcessed = 0L;
    @Column(name = "items_failed") private Long itemsFailed = 0L;

    @Column(name = "started_at") private OffsetDateTime startedAt;
    @Column(name = "finished_at") private OffsetDateTime finishedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parameters", columnDefinition = "jsonb")
    private Map<String, Object> parameters;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_metadata", columnDefinition = "jsonb")
    private Map<String, Object> resultMetadata;

    @Column(name = "result_location", length = 1000) private String resultLocation;

    @Column(name = "error_code", length = 80) private String errorCode;
    @Column(name = "error_message", length = 4000) private String errorMessage;

    @Column(name = "retention_until") private OffsetDateTime retentionUntil;

    @Version
    @Column(name = "version") private Long version;

    public enum Status { QUEUED, RUNNING, COMPLETED, FAILED, CANCELLED, EXPIRED }
}
