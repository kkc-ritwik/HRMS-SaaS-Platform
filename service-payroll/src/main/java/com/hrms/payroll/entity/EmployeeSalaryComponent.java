package com.hrms.payroll.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Per-employee breakdown: how much each component pays in a specific salary revision.
 * Does not extend BaseEntity (no tenant_id / audit columns in this table).
 */
@Entity
@Table(name = "employee_salary_components")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("EmployeeSalaryComponent")
@EntityListeners(AuditEntityListener.class)
public class EmployeeSalaryComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "employee_salary_id", nullable = false)
    private UUID employeeSalaryId;

    @Column(name = "component_id", nullable = false)
    private UUID componentId;

    @Column(name = "monthly_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyAmount = BigDecimal.ZERO;

    @Column(name = "annual_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal annualAmount = BigDecimal.ZERO;

    @Column(name = "employer_contribution", nullable = false, precision = 12, scale = 2)
    private BigDecimal employerContribution = BigDecimal.ZERO;
}
