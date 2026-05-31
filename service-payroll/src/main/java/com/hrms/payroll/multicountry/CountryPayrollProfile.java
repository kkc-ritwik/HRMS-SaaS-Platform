package com.hrms.payroll.multicountry;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Map;

/**
 * Country-specific payroll configuration — slab rates, statutory contribution percentages,
 * mandatory components, public-holiday-pay rules. One row per (country, financial-year).
 *
 * Examples:
 *   India       → PF 12%, ESI 0.75% (emp) / 3.25% (er), PT slabs per state, NPS optional
 *   USA         → FICA 6.2% (SS) + 1.45% (Medicare), Federal withholding tables, state tax
 *   UK          → NI Class 1, PAYE, Auto-enrol pension
 *   Australia   → Superannuation 11.5%, PAYG, Medicare levy
 *   Singapore   → CPF (employer + employee % varies by age + residency)
 *   UAE         → Gratuity 21-day rule for ≤5y, 30-day rule for &gt;5y, no income tax
 *
 * The tax_brackets JSON is keyed by component (income_tax, pf, esi, etc.) — each value
 * is a list of {{min, max, rate, fixed}} slabs.
 */
@Entity
@Table(name = "payroll_country_profiles",
        uniqueConstraints = @UniqueConstraint(name = "uq_country_fy", columnNames = {"country","financial_year"}),
        indexes = @Index(name = "ix_country_fy", columnList = "country,financial_year"))
@Auditable("CountryPayrollProfile")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CountryPayrollProfile extends BaseEntity {

    @Column(name = "country", length = 2, nullable = false) private String country;
    @Column(name = "country_name", length = 100) private String countryName;
    @Column(name = "financial_year", length = 10, nullable = false) private String financialYear;
    @Column(name = "fy_start") private LocalDate fyStart;
    @Column(name = "fy_end")   private LocalDate fyEnd;

    @Column(name = "currency", length = 3, nullable = false) private String currency;
    @Column(name = "standard_deduction", precision = 12, scale = 2) private java.math.BigDecimal standardDeduction;
    @Column(name = "pf_employer_percent", precision = 5, scale = 2) private java.math.BigDecimal pfEmployerPercent;
    @Column(name = "pf_employee_percent", precision = 5, scale = 2) private java.math.BigDecimal pfEmployeePercent;
    @Column(name = "pf_wage_ceiling", precision = 12, scale = 2) private java.math.BigDecimal pfWageCeiling;
    @Column(name = "esi_employer_percent", precision = 5, scale = 2) private java.math.BigDecimal esiEmployerPercent;
    @Column(name = "esi_employee_percent", precision = 5, scale = 2) private java.math.BigDecimal esiEmployeePercent;
    @Column(name = "esi_wage_ceiling", precision = 12, scale = 2) private java.math.BigDecimal esiWageCeiling;
    @Column(name = "gratuity_eligibility_years") private Integer gratuityEligibilityYears;

    /** Component → list of slabs [{min, max, rate, fixed}]. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tax_brackets", columnDefinition = "jsonb")
    private Map<String, Object> taxBrackets;

    /** Country-specific overrides — { socialSecurity, medicareLevy, lwf, pt, etc. } */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "additional_components", columnDefinition = "jsonb")
    private Map<String, Object> additionalComponents;

    @Column(name = "active") private Boolean active = true;
}
