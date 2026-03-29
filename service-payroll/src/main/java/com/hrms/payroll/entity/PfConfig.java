package com.hrms.payroll.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pf_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PfConfig extends BaseEntity {

    @Column(name = "pf_number", length = 50)
    private String pfNumber;

    /** Wage ceiling for PF applicability (₹15,000 as per Indian law). */
    @Column(name = "basic_wage_ceiling", nullable = false, precision = 10, scale = 2)
    private BigDecimal basicWageCeiling = BigDecimal.valueOf(15000);

    /** Employee PF contribution rate (%) — default 12%. */
    @Column(name = "pf_rate_employee", nullable = false, precision = 6, scale = 4)
    private BigDecimal pfRateEmployee = BigDecimal.valueOf(12);

    /** Employer PF contribution rate (%) — default 12%. */
    @Column(name = "pf_rate_employer", nullable = false, precision = 6, scale = 4)
    private BigDecimal pfRateEmployer = BigDecimal.valueOf(12);

    /** Employees' Pension Scheme rate (%) — default 8.33%. */
    @Column(name = "eps_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal epsRate = BigDecimal.valueOf(8.33);

    /** Employees' Deposit Linked Insurance rate (%) — default 0.5%. */
    @Column(name = "edli_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal edliRate = BigDecimal.valueOf(0.5);

    /** EPFO admin charge rate (%) — default 0.5%. */
    @Column(name = "admin_charge_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal adminChargeRate = BigDecimal.valueOf(0.5);

    /** Whether employer PF is included in CTC. */
    @Column(name = "include_employer_pf_in_ctc", nullable = false)
    private boolean includeEmployerPfInCtc = true;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;
}
