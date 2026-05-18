package com.hrms.leave.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "regularization_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("RegularizationRequest")
@EntityListeners(AuditEntityListener.class)
public class RegularizationRequest extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "original_in")
    private Instant originalIn;

    @Column(name = "original_out")
    private Instant originalOut;

    @Column(name = "corrected_in", nullable = false)
    private Instant correctedIn;

    @Column(name = "corrected_out", nullable = false)
    private Instant correctedOut;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RegularizationStatus status = RegularizationStatus.PENDING;

    @Column(name = "approved_by")
    private UUID approvedBy;

    public enum RegularizationStatus {
        PENDING, APPROVED, REJECTED
    }
}
