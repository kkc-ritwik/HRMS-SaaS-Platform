package com.hrms.payroll.bonus;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Map;

/**
 * One-time / variable-pay bonus cycle (annual bonus, festival bonus, retention bonus,
 * spot award batch). Individual employee allocations live in BonusAllocation; payouts
 * are then injected as one-time components into the next payroll run.
 */
@Entity
@Table(name = "bonus_cycles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("BonusCycle")
@EntityListeners(AuditEntityListener.class)
public class BonusCycle extends BaseEntity {

    @Column(name = "name", length = 200, nullable = false) private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "bonus_type", length = 30, nullable = false)
    private BonusType bonusType;

    @Column(name = "payout_month", nullable = false) private LocalDate payoutMonth;
    @Column(name = "fiscal_year", length = 9) private String fiscalYear;

    /** Optional formula expression evaluated per employee. e.g. "rating * 0.05 * annualBase / 12" */
    @Column(name = "formula_expression", length = 500) private String formulaExpression;

    /** Default amount used when formula doesn't apply / for spot bonuses. */
    @Column(name = "default_amount", precision = 14, scale = 2) private java.math.BigDecimal defaultAmount;

    /** { ratingKey -> multiplier } for performance-linked bonuses. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rating_multiplier", columnDefinition = "jsonb")
    private Map<String, java.math.BigDecimal> ratingMultiplier;

    @Column(name = "is_taxable") private Boolean taxable;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status = Status.DRAFT;

    public enum BonusType { ANNUAL, FESTIVAL, JOINING, RETENTION, REFERRAL, PERFORMANCE, SPOT, STATUTORY }
    public enum Status { DRAFT, OPEN, APPROVED, PAID, CANCELLED }
}
