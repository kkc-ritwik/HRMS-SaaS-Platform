package com.hrms.tenant.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.UUID;

@MappedSuperclass @Getter @Setter
public abstract class BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private String tenantId;
    @Column(name = "created_by") private String createdBy;
    @Column(name = "updated_by") private String updatedBy;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at") private Instant updatedAt;
    @Column(name = "is_deleted") private boolean deleted = false;
}
