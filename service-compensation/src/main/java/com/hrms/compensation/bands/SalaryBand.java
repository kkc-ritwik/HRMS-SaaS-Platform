package com.hrms.compensation.bands;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Salary band for a pay grade. Defines min / mid / max with mid-point as the
 * reference. Used by compensation planning, increment cycles, and offer-recommendation engines.
 */
@Entity
@Table(name = "salary_bands",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","pay_grade_id","country"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SalaryBand extends BaseEntity {

    @Column(name = "pay_grade_id", nullable = false)
    private UUID payGradeId;

    @Column(name = "country", length = 2, nullable = false)
    private String country;          // ISO 3166-1 alpha-2

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;         // ISO 4217

    @Column(name = "min_amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal minAmount;

    @Column(name = "mid_amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal midAmount;

    @Column(name = "max_amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal maxAmount;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /** Compa-ratio = actual / mid. 1.0 = at mid, 0.8 = below band, 1.2 = above band. */
    public BigDecimal compaRatio(BigDecimal actual) {
        if (actual == null || midAmount == null || midAmount.signum() == 0) return BigDecimal.ZERO;
        return actual.divide(midAmount, 4, java.math.RoundingMode.HALF_UP);
    }
}
