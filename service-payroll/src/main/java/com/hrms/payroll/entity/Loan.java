package com.hrms.payroll.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.events.annotation.PublishEvents;
import com.hrms.events.listener.EntityLifecyclePublisher;
import com.hrms.events.model.Topics;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "loans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("Loan")
@PublishEvents(topic = Topics.PAYROLL, namespace = "payroll.loan")
@EntityListeners({AuditEntityListener.class, EntityLifecyclePublisher.class})
public class Loan extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    /** HOME, PERSONAL, VEHICLE, SALARY_ADVANCE, etc. */
    @Column(name = "loan_type", nullable = false, length = 50)
    private String loanType;

    @Column(name = "principal_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal principalAmount;

    /** Annual interest rate in percent. 0 = interest-free salary advance. */
    @Column(name = "interest_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal interestRate = BigDecimal.ZERO;

    @Column(name = "tenure_months", nullable = false)
    private int tenureMonths;

    @Column(name = "emi_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "disbursement_date", nullable = false)
    private LocalDate disbursementDate;

    /** First payroll month from which EMI is deducted. */
    @Column(name = "start_deduction_month", nullable = false)
    private LocalDate startDeductionMonth;

    @Column(name = "outstanding_balance", nullable = false, precision = 14, scale = 2)
    private BigDecimal outstandingBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LoanStatus status = LoanStatus.ACTIVE;

    // â”€â”€ Enum â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public enum LoanStatus { ACTIVE, CLOSED, DEFAULTED }
}
