package com.hrms.payroll.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payroll_runs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PayrollRun extends BaseEntity {

    @Column(name = "month", nullable = false)
    private int month;

    @Column(name = "year", nullable = false)
    private int year;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RunStatus status = RunStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "run_type", nullable = false, length = 20)
    private RunType runType = RunType.REGULAR;

    @Column(name = "total_gross", precision = 16, scale = 2)
    private BigDecimal totalGross = BigDecimal.ZERO;

    @Column(name = "total_deductions", precision = 16, scale = 2)
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "total_net", precision = 16, scale = 2)
    private BigDecimal totalNet = BigDecimal.ZERO;

    @Column(name = "total_employer_pf", precision = 14, scale = 2)
    private BigDecimal totalEmployerPf = BigDecimal.ZERO;

    @Column(name = "total_employer_esi", precision = 14, scale = 2)
    private BigDecimal totalEmployerEsi = BigDecimal.ZERO;

    @Column(name = "employee_count")
    private int employeeCount = 0;

    @Column(name = "processed_by", length = 100)
    private String processedBy;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "locked_by", length = 100)
    private String lockedBy;

    @Column(name = "locked_at")
    private Instant lockedAt;

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum RunStatus {
        DRAFT, PROCESSING, PROCESSED, LOCKED, PAID
    }

    public enum RunType {
        REGULAR, SUPPLEMENTARY, ARREARS, FNF
    }
}
