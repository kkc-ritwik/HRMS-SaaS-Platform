package com.hrms.payroll.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employee_salary")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EmployeeSalary extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "salary_structure_id")
    private UUID salaryStructureId;

    @Column(name = "ctc_annual", nullable = false, precision = 14, scale = 2)
    private BigDecimal ctcAnnual;

    @Column(name = "gross_monthly", precision = 12, scale = 2)
    private BigDecimal grossMonthly;

    @Column(name = "net_monthly", precision = 12, scale = 2)
    private BigDecimal netMonthly;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "revision_letter_url", length = 1000)
    private String revisionLetterUrl;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SalaryStatus status = SalaryStatus.ACTIVE;

    public enum SalaryStatus {
        ACTIVE, INACTIVE
    }
}
