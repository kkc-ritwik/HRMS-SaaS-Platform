package com.hrms.files.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "file_folders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Auditable("FileFolder")
@EntityListeners(AuditEntityListener.class)
public class FileFolder {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "owner_id", nullable = false) private UUID ownerId;
    @Column(name = "parent_id") private UUID parentId;
    @Column(nullable = false, length = 200) private String name;
    @Column(name = "is_shared") private Boolean isShared;
    @Column(name = "created_at") private OffsetDateTime createdAt;
    @PrePersist void prePersist() { createdAt = OffsetDateTime.now(); }
}
