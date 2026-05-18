package com.hrms.files.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "file_shares", indexes = @Index(name = "ix_share_file", columnList = "file_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Auditable("FileShare")
@EntityListeners(AuditEntityListener.class)
public class FileShare {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "file_id", nullable = false) private UUID fileId;
    @Column(name = "shared_with_user_id") private UUID sharedWithUserId;
    @Column(name = "shared_with_group_id") private UUID sharedWithGroupId;
    @Enumerated(EnumType.STRING) @Column(length = 20) private Permission permission;
    @Column(name = "share_token", length = 100, unique = true) private String shareToken;
    @Column(name = "expires_at") private OffsetDateTime expiresAt;
    @Column(name = "created_at") private OffsetDateTime createdAt;
    @PrePersist void prePersist() { createdAt = OffsetDateTime.now(); }
    public enum Permission { VIEW, COMMENT, EDIT, OWNER }
}
