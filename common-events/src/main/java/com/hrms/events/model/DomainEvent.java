package com.hrms.events.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DomainEvent {
    /** Globally unique event id (idempotency key for consumers). */
    private String eventId;
    /** e.g. "employee.created", "leave.approved", "payslip.published" */
    private String eventType;
    /** e.g. "core-hr", "leave-attendance" */
    private String sourceService;
    private String tenantId;
    /** Logical aggregate id (employee_id, leave_app_id…) */
    private String aggregateId;
    private String aggregateType;
    private OffsetDateTime occurredAt;
    /** Optional actor for traceability. */
    private String actorId;
    /** Correlation/trace id for cross-service follow-up. */
    private String correlationId;
    /** Free-form payload. */
    private Map<String, Object> payload;
    /** Schema version for forward-compat. */
    private Integer schemaVersion;

    public static DomainEvent of(String type, String source, String tenantId, String aggregateId,
                                 String aggregateType, Map<String, Object> payload) {
        return DomainEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(type).sourceService(source).tenantId(tenantId)
                .aggregateId(aggregateId).aggregateType(aggregateType)
                .occurredAt(OffsetDateTime.now()).schemaVersion(1).payload(payload).build();
    }
}
