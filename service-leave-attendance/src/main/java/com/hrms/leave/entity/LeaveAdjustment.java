package com.hrms.leave.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leave_adjustments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("LeaveAdjustment")
@EntityListeners(AuditEntityListener.class)
public class LeaveAdjustment extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "leave_type_id", nullable = false)
    private UUID leaveTypeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", nullable = false, length = 10)
    private AdjustmentType adjustmentType;

    @Column(name = "days", nullable = false, precision = 6, scale = 2)
    private BigDecimal days;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "adjusted_by", length = 100)
    private String adjustedBy;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    public enum AdjustmentType {
        CREDIT, DEBIT
    }
}
