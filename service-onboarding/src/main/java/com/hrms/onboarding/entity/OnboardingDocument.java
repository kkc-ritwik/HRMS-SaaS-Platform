package com.hrms.onboarding.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "onboarding_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Auditable("OnboardingDocument")
@EntityListeners(AuditEntityListener.class)
public class OnboardingDocument extends BaseEntity {

    public enum DocumentStatus {
        PENDING, SUBMITTED, APPROVED, REJECTED
    }

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "document_name", nullable = false, length = 200)
    private String documentName;

    @Column(name = "document_type", length = 100)
    private String documentType;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.PENDING;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
