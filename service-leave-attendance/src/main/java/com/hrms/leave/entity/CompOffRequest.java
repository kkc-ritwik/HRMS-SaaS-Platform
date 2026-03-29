package com.hrms.leave.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "comp_off_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CompOffRequest extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "worked_date", nullable = false)
    private LocalDate workedDate;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CompOffStatus status = CompOffStatus.PENDING;

    @Column(name = "approved_by")
    private UUID approvedBy;

    public enum CompOffStatus {
        PENDING, APPROVED, REJECTED
    }
}
