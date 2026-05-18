package com.hrms.engagement.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "polls")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Auditable("Poll")
@EntityListeners(AuditEntityListener.class)
public class Poll {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(nullable = false, length = 500) private String question;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb") private List<String> options;
    @Column(name = "allow_multi") private Boolean allowMulti;
    @Column(name = "is_anonymous") private Boolean isAnonymous;
    @Column(name = "closes_at") private OffsetDateTime closesAt;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "created_at") private OffsetDateTime createdAt;
    @PrePersist void prePersist() { createdAt = OffsetDateTime.now(); }
}
