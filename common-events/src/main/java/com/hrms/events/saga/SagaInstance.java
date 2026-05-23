package com.hrms.events.saga;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Saga instance — orchestrates a multi-service business transaction that can't be wrapped
 * in a 2PC. Example: onboarding = createEmployee → provisionEmail → provisionAssets →
 * sendWelcomeKit; if step 3 fails the saga compensates back through step 2 and 1.
 *
 *   steps[]      = ordered list of step descriptors  { name, service, command, status, payload }
 *   currentStep  = pointer into steps[]
 *   compensating = true if currently running compensations
 *   trace        = each step's outcome with timestamps
 *
 * The {@link SagaOrchestrator} consumes Kafka command-replies and advances the saga.
 */
@Entity
@Table(name = "saga_instances",
        indexes = {
                @Index(name = "ix_saga_status", columnList = "tenant_id,status"),
                @Index(name = "ix_saga_name",   columnList = "tenant_id,name")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SagaInstance {

    @Id @GeneratedValue private UUID id;

    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "name", length = 100, nullable = false) private String name;          // "onboarding", "payroll-run", "separation"
    @Column(name = "subject_id", length = 100) private String subjectId;                  // employeeId / payrollRunId / etc.

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.STARTED;

    @Column(name = "current_step") private Integer currentStep = 0;
    @Column(name = "compensating") private Boolean compensating = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "steps", columnDefinition = "jsonb")
    private List<Map<String, Object>> steps;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context", columnDefinition = "jsonb")
    private Map<String, Object> context;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "trace", columnDefinition = "jsonb")
    private List<Map<String, Object>> trace;

    @Column(name = "started_at") private OffsetDateTime startedAt;
    @Column(name = "finished_at") private OffsetDateTime finishedAt;
    @Column(name = "error_message", length = 2000) private String errorMessage;

    @Version
    @Column(name = "version") private Long version;

    @PrePersist void onCreate() { if (startedAt == null) startedAt = OffsetDateTime.now(); }

    public enum Status { STARTED, RUNNING, COMPLETED, COMPENSATING, COMPENSATED, FAILED }
}
