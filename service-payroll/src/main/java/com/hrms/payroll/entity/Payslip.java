package com.hrms.payroll.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "payslips")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Payslip extends BaseEntity {

    @Column(name = "payroll_run_id", nullable = false)
    private UUID payrollRunId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "month", nullable = false)
    private int month;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "days_in_month", nullable = false)
    private int daysInMonth = 30;

    @Column(name = "days_payable", nullable = false)
    private int daysPayable = 30;

    @Column(name = "days_worked", nullable = false)
    private int daysWorked = 30;

    @Column(name = "lop_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal lopDays = BigDecimal.ZERO;

    @Column(name = "gross_earnings", nullable = false, precision = 14, scale = 2)
    private BigDecimal grossEarnings = BigDecimal.ZERO;

    @Column(name = "total_deductions", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "net_pay", nullable = false, precision = 14, scale = 2)
    private BigDecimal netPay = BigDecimal.ZERO;

    @Column(name = "employer_pf", nullable = false, precision = 12, scale = 2)
    private BigDecimal employerPf = BigDecimal.ZERO;

    @Column(name = "employer_esi", nullable = false, precision = 12, scale = 2)
    private BigDecimal employerEsi = BigDecimal.ZERO;

    @Column(name = "employer_lwf", nullable = false, precision = 10, scale = 2)
    private BigDecimal employerLwf = BigDecimal.ZERO;

    @Column(name = "employee_pf", nullable = false, precision = 12, scale = 2)
    private BigDecimal employeePf = BigDecimal.ZERO;

    @Column(name = "employee_esi", nullable = false, precision = 12, scale = 2)
    private BigDecimal employeeEsi = BigDecimal.ZERO;

    @Column(name = "employee_pt", nullable = false, precision = 10, scale = 2)
    private BigDecimal employeePt = BigDecimal.ZERO;

    @Column(name = "tds", nullable = false, precision = 12, scale = 2)
    private BigDecimal tds = BigDecimal.ZERO;

    /** Full component-by-component breakdown stored as JSONB. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "components_json", columnDefinition = "jsonb")
    private List<PayslipLineItem> componentsJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PayslipStatus status = PayslipStatus.DRAFT;

    @Column(name = "pdf_url", length = 1000)
    private String pdfUrl;

    @Column(name = "emailed_at")
    private Instant emailedAt;

    // ── Enum ──────────────────────────────────────────────────────────────────

    public enum PayslipStatus {
        DRAFT, PROCESSED, LOCKED, PUBLISHED
    }
}
