package com.hrms.corehr.costcenter;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Cost center master — hierarchical accounting bucket employee/project costs roll up to.
 * Used by Finance for departmental budget tracking, transfer pricing, and inter-company
 * recharges. An employee can be 100% allocated to one cost centre or split-allocated
 * across several (see {@link CostCenterAllocation}).
 */
@Entity
@Table(name = "cost_centers",
        uniqueConstraints = @UniqueConstraint(name = "uq_cc_code", columnNames = {"tenant_id","code"}),
        indexes = @Index(name = "ix_cc_parent", columnList = "tenant_id,parent_id"))
@Auditable("CostCenter")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CostCenter extends BaseEntity {

    @Column(name = "code", length = 50, nullable = false) private String code;
    @Column(name = "name", length = 200, nullable = false) private String name;
    @Column(name = "parent_id") private UUID parentId;

    @Column(name = "department_id") private UUID departmentId;
    @Column(name = "location_id")   private UUID locationId;
    @Column(name = "legal_entity_id") private UUID legalEntityId;
    @Column(name = "manager_employee_id") private UUID managerEmployeeId;

    @Column(name = "currency", length = 3) private String currency;
    @Column(name = "annual_budget", precision = 14, scale = 2) private BigDecimal annualBudget;

    @Column(name = "active") private Boolean active = true;
}
