package com.hrms.document.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.events.annotation.PublishEvents;
import com.hrms.events.listener.EntityLifecyclePublisher;
import com.hrms.events.model.Topics;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Auditable("Document")
@PublishEvents(topic = Topics.DOCUMENT, namespace = "document")
@EntityListeners({AuditEntityListener.class, EntityLifecyclePublisher.class})
public class Document extends BaseEntity {

    public enum DocumentStatus {
        DRAFT, SUBMITTED, VERIFIED, REJECTED, EXPIRED
    }

    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(name = "document_type_id")
    private UUID documentTypeId;

    @Column(name = "file_name", nullable = false, length = 300)
    private String fileName;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DocumentStatus status = DocumentStatus.DRAFT;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "uploaded_by")
    private UUID uploadedBy;
}
