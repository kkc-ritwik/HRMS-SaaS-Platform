package com.hrms.forms.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "form_definitions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","code","version"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FormDefinition {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(nullable = false, length = 100) private String code;
    @Column(nullable = false, length = 200) private String title;
    @Column(length = 1000) private String description;
    @Column(nullable = false) private Integer version;
    @Enumerated(EnumType.STRING) @Column(length = 30) private Status status;

    /** Field schema â€” array of { name, label, type, required, options, validators, defaultValue } */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> fields;

    /** Workflow trigger config â€” { onSubmit: { workflowCode, notificationChannel, recipients } } */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "workflow_config", columnDefinition = "jsonb")
    private Map<String, Object> workflowConfig;

    @Column(name = "is_anonymous_allowed") private Boolean isAnonymousAllowed;
    @Column(name = "is_active") private Boolean isActive;
    @Column(name = "created_at") private OffsetDateTime createdAt;
    @Column(name = "updated_at") private OffsetDateTime updatedAt;

    @PrePersist void prePersist() { createdAt = OffsetDateTime.now(); updatedAt = createdAt; }
    @PreUpdate  void preUpdate()  { updatedAt = OffsetDateTime.now(); }
    public enum Status { DRAFT, PUBLISHED, ARCHIVED }
}
