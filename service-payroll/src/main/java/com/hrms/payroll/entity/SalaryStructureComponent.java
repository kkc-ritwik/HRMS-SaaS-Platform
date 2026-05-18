package com.hrms.payroll.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Join table: which components belong to a salary structure and their default values.
 * Does not extend BaseEntity (no tenant_id / audit columns in this table).
 */
@Entity
@Table(name = "salary_structure_components")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("SalaryStructureComponent")
@EntityListeners(AuditEntityListener.class)
public class SalaryStructureComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "salary_structure_id", nullable = false)
    private UUID salaryStructureId;

    @Column(name = "component_id", nullable = false)
    private UUID componentId;

    @Column(name = "default_amount", precision = 12, scale = 2)
    private BigDecimal defaultAmount;

    @Column(name = "default_percentage", precision = 10, scale = 4)
    private BigDecimal defaultPercentage;

    @Column(name = "is_mandatory", nullable = false)
    private boolean mandatory = true;
}
