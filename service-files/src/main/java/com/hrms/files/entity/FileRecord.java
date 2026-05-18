package com.hrms.files.entity;

import com.hrms.audit.annotation.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "file_records", indexes = {
    @Index(name = "ix_file_owner", columnList = "tenant_id,owner_id"),
    @Index(name = "ix_file_folder", columnList = "tenant_id,folder_id")
})
@Auditable("File")
@EntityListeners(com.hrms.audit.listener.AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FileRecord {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "owner_id", nullable = false) private UUID ownerId;
    @Column(name = "folder_id") private UUID folderId;
    @Column(nullable = false, length = 500) private String filename;
    @Column(name = "content_type", length = 100) private String contentType;
    @Column(name = "size_bytes") private Long sizeBytes;
    @Column(name = "storage_uri", length = 1000, nullable = false) private String storageUri;
    @Column(name = "checksum_sha256", length = 64) private String checksumSha256;
    @Column(name = "is_public") private Boolean isPublic;
    @Column(length = 1000) private String description;
    @Column(length = 500) private String tags;
    @Column(name = "created_at") private OffsetDateTime createdAt;
    @Column(name = "updated_at") private OffsetDateTime updatedAt;

    @PrePersist void prePersist() { createdAt = OffsetDateTime.now(); updatedAt = createdAt; }
    @PreUpdate  void preUpdate()  { updatedAt = OffsetDateTime.now(); }
}
