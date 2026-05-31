package com.hrms.corehr.costcenter;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Split allocation: employee X is 60% on cost-centre A and 40% on cost-centre B between
 * dates D1..D2. Payroll uses these weights to distribute salary cost; Finance uses them
 * for budget reporting. Sum of percentages per employee per overlapping period must = 100.
 */
@Entity
@Table(name = "cost_center_allocations",
        indexes = {
            @Index(name = "ix_cca_employee", columnList = "tenant_id,employee_id"),
            @Index(name = "ix_cca_cc",       columnList = "tenant_id,cost_center_id")
        })
@Auditable("CostCenterAllocation")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CostCenterAllocation extends BaseEntity {

    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "cost_center_id", nullable = false) private UUID costCenterId;

    @Column(name = "percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal percentage;

    @Column(name = "effective_from", nullable = false) private LocalDate effectiveFrom;
    @Column(name = "effective_to") private LocalDate effectiveTo;

    @Column(name = "approved_by") private UUID approvedBy;
    @Column(name = "notes", length = 500) private String notes;
}
