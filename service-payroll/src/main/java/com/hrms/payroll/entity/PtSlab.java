package com.hrms.payroll.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pt_slabs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("PtSlab")
@EntityListeners(AuditEntityListener.class)
public class PtSlab extends BaseEntity {

    @Column(name = "state", nullable = false, length = 50)
    private String state;

    /** Lower bound of the monthly salary range (inclusive). */
    @Column(name = "slab_from", nullable = false, precision = 10, scale = 2)
    private BigDecimal slabFrom = BigDecimal.ZERO;

    /** Upper bound of the monthly salary range (inclusive). NULL = no ceiling. */
    @Column(name = "slab_to", precision = 10, scale = 2)
    private BigDecimal slabTo;

    /** PT amount deducted per month for salaries in this range. */
    @Column(name = "monthly_tax", nullable = false, precision = 8, scale = 2)
    private BigDecimal monthlyTax;

    /** 'M', 'F', or NULL (applies to all genders). */
    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;
}
