package com.hrms.leave.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "overtime_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OvertimeRecord extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "date", nullable = false)
    private LocalDate overtimeDate;

    @Column(name = "ot_hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal otHours;

    @Column(name = "ot_rate_multiplier", nullable = false, precision = 4, scale = 2)
    private BigDecimal otRateMultiplier = BigDecimal.valueOf(1.5);

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OvertimeStatus status = OvertimeStatus.PENDING;

    @Column(name = "approved_by")
    private UUID approvedBy;

    public enum OvertimeStatus {
        PENDING, APPROVED, REJECTED
    }
}
