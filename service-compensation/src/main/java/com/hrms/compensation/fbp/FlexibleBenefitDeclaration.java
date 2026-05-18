package com.hrms.compensation.fbp;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Flexible Benefit Plan declaration â€” employee allocates their annual FBP pool
 * across optional tax-saving components: LTA, Fuel, Phone, Meal Card, etc.
 * Once locked, payroll uses these allocations to structure monthly payslip components.
 */
@Entity
@Table(name = "fbp_declarations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","employee_id","financial_year"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FlexibleBenefitDeclaration extends BaseEntity {

    @Column(name = "employee_id", nullable = false) private UUID employeeId;

    /** "2025-26" */
    @Column(name = "financial_year", length = 9, nullable = false) private String financialYear;

    @Column(name = "annual_pool", precision = 14, scale = 2, nullable = false)
    private BigDecimal annualPool;

    /** Map of { componentCode -> annualAmount } the employee has allocated. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allocations", columnDefinition = "jsonb", nullable = false)
    private Map<String, BigDecimal> allocations;

    @Column(name = "total_allocated", precision = 14, scale = 2)
    private BigDecimal totalAllocated;

    @Column(name = "unallocated_to_special", precision = 14, scale = 2)
    private BigDecimal unallocatedToSpecial;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status = Status.DRAFT;

    @Column(name = "locked_at") private java.time.Instant lockedAt;

    public enum Status { DRAFT, SUBMITTED, APPROVED, LOCKED }
}
