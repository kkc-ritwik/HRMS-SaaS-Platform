package com.hrms.payroll.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tax_declarations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TaxDeclaration extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    /** E.g. "2025-26". */
    @Column(name = "financial_year", nullable = false, length = 10)
    private String financialYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "regime", nullable = false, length = 5)
    private TaxRegime regime = TaxRegime.NEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeclarationStatus status = DeclarationStatus.DRAFT;

    /** Investments under Section 80C — max exemption ₹1,50,000. */
    @Column(name = "section_80c", nullable = false, precision = 12, scale = 2)
    private BigDecimal section80c = BigDecimal.ZERO;

    /** Medical insurance premiums — max exemption ₹25,000 (self) + ₹50,000 (senior parents). */
    @Column(name = "section_80d", nullable = false, precision = 12, scale = 2)
    private BigDecimal section80d = BigDecimal.ZERO;

    /** Education loan interest — no upper limit. */
    @Column(name = "section_80e", nullable = false, precision = 12, scale = 2)
    private BigDecimal section80e = BigDecimal.ZERO;

    /** Donations — as per applicable limits. */
    @Column(name = "section_80g", nullable = false, precision = 12, scale = 2)
    private BigDecimal section80g = BigDecimal.ZERO;

    /** Home loan interest (self-occupied) — max ₹2,00,000. */
    @Column(name = "section_24b", nullable = false, precision = 12, scale = 2)
    private BigDecimal section24b = BigDecimal.ZERO;

    /** HRA exemption calculated and claimed. */
    @Column(name = "hra_exemption_claimed", nullable = false, precision = 12, scale = 2)
    private BigDecimal hraExemptionClaimed = BigDecimal.ZERO;

    /** LTA already claimed (block of two years). */
    @Column(name = "lta_claimed", nullable = false, precision = 12, scale = 2)
    private BigDecimal ltaClaimed = BigDecimal.ZERO;

    /** Other income (interest, rental, etc.) to be included in taxable computation. */
    @Column(name = "other_income", nullable = false, precision = 12, scale = 2)
    private BigDecimal otherIncome = BigDecimal.ZERO;

    /** Income from previous employer in the same financial year. */
    @Column(name = "previous_employer_income", nullable = false, precision = 14, scale = 2)
    private BigDecimal previousEmployerIncome = BigDecimal.ZERO;

    /** TDS already deducted by previous employer. */
    @Column(name = "previous_employer_tds", nullable = false, precision = 12, scale = 2)
    private BigDecimal previousEmployerTds = BigDecimal.ZERO;

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum TaxRegime { OLD, NEW }

    public enum DeclarationStatus { DRAFT, SUBMITTED, VERIFIED }
}
