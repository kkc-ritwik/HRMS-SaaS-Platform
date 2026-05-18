package com.hrms.lms.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "certifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Auditable("Certification")
@EntityListeners(AuditEntityListener.class)
public class Certification extends BaseEntity {

    public enum CertificationStatus { ACTIVE, EXPIRED, REVOKED }

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "certificate_name", nullable = false, length = 300)
    private String certificateName;

    @Column(name = "issued_by", length = 200)
    private String issuedBy;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "certificate_url", length = 500)
    private String certificateUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CertificationStatus status = CertificationStatus.ACTIVE;
}
