package com.hrms.leave.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "leave_balances")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LeaveBalance extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "leave_type_id", nullable = false)
    private UUID leaveTypeId;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "opening_balance", nullable = false, precision = 8, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(name = "accrued", nullable = false, precision = 8, scale = 2)
    private BigDecimal accrued = BigDecimal.ZERO;

    @Column(name = "used", nullable = false, precision = 8, scale = 2)
    private BigDecimal used = BigDecimal.ZERO;

    @Column(name = "carry_forward", nullable = false, precision = 8, scale = 2)
    private BigDecimal carryForward = BigDecimal.ZERO;

    @Column(name = "encashed", nullable = false, precision = 8, scale = 2)
    private BigDecimal encashed = BigDecimal.ZERO;

    @Column(name = "adjusted", nullable = false, precision = 8, scale = 2)
    private BigDecimal adjusted = BigDecimal.ZERO;

    /**
     * Computed/stored column in DB: opening + accrued + carry_forward + adjusted - used - encashed.
     * Marked insertable=false, updatable=false so JPA never writes it — Postgres manages it.
     */
    @Column(name = "available", insertable = false, updatable = false, precision = 8, scale = 2)
    private BigDecimal available;
}
