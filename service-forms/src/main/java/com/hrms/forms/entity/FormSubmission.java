package com.hrms.forms.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "form_submissions",
        indexes = {
            @Index(name = "ix_sub_form", columnList = "form_id"),
            @Index(name = "ix_sub_submitter", columnList = "tenant_id,submitter_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FormSubmission {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "form_id", nullable = false) private UUID formId;
    @Column(name = "submitter_id") private UUID submitterId;
    @Column(name = "is_anonymous") private Boolean isAnonymous;

    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb")
    private Map<String, Object> answers;

    @Column(name = "workflow_instance_id") private UUID workflowInstanceId;
    @Column(name = "submitted_at", nullable = false) private OffsetDateTime submittedAt;

    @PrePersist void prePersist() { if (submittedAt == null) submittedAt = OffsetDateTime.now(); }
}
