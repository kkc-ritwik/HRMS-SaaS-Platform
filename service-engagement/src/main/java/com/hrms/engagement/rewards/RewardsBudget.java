package com.hrms.engagement.rewards;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Per-manager / per-department rewards budget pool. Used by manager-discretionary
 * spot awards. Each budget has an allocated amount, a consumed running total, and a
 * validity window. Approval gates and depletion alerts (75% / 90% / 100%) are fired
 * by RewardsBudgetService on each spend.
 */
@Entity
@Table(name = "engagement_rewards_budgets", indexes = {
        @Index(name = "ix_rbudget_owner", columnList = "tenant_id,owner_manager_id"),
        @Index(name = "ix_rbudget_dept", columnList = "tenant_id,department_id")
})
@Auditable("RewardsBudget")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RewardsBudget extends BaseEntity {

    @Column(name = "name", length = 200, nullable = false) private String name;
    @Column(name = "owner_manager_id") private UUID ownerManagerId;
    @Column(name = "department_id") private UUID departmentId;

    @Column(name = "fiscal_year", length = 10) private String fiscalYear;
    @Column(name = "period_start", nullable = false) private LocalDate periodStart;
    @Column(name = "period_end", nullable = false) private LocalDate periodEnd;

    @Column(name = "allocated_amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal allocatedAmount;
    @Column(name = "consumed_amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal consumedAmount = BigDecimal.ZERO;
    @Column(name = "currency", length = 3, nullable = false) private String currency = "INR";

    @Column(name = "allow_overdraft") private Boolean allowOverdraft = false;
    @Column(name = "active") private Boolean active = true;
}
