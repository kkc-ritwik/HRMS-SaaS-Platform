package com.hrms.payroll.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * One EMI instalment recorded when a payslip is generated or manual payment is made.
 */
@Entity
@Table(name = "loan_repayments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LoanRepayment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "loan_id", nullable = false)
    private UUID loanId;

    /** Null for manual (off-payroll) repayments. */
    @Column(name = "payslip_id")
    private UUID payslipId;

    @Column(name = "emi_number", nullable = false)
    private int emiNumber;

    @Column(name = "principal_part", nullable = false, precision = 12, scale = 2)
    private BigDecimal principalPart = BigDecimal.ZERO;

    @Column(name = "interest_part", nullable = false, precision = 12, scale = 2)
    private BigDecimal interestPart = BigDecimal.ZERO;

    @Column(name = "total_emi", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalEmi = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "paid_at", nullable = false, updatable = false)
    private Instant paidAt;
}
