package com.hrms.compliance.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "licenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class License extends BaseEntity {

    public enum LicenseStatus {
        ACTIVE, EXPIRED, REVOKED, PENDING_RENEWAL
    }

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "license_type", nullable = false, length = 100)
    private String licenseType;

    @Column(name = "license_number", length = 100)
    private String licenseNumber;

    @Column(name = "issuing_authority", length = 200)
    private String issuingAuthority;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private LicenseStatus status = LicenseStatus.ACTIVE;

    @Column(name = "renewal_reminder_days", nullable = false)
    private int renewalReminderDays = 30;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
