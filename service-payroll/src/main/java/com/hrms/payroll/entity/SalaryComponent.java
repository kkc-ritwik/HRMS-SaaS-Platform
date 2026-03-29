package com.hrms.payroll.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "salary_components")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SalaryComponent extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private ComponentType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_type", nullable = false, length = 20)
    private CalculationType calculationType = CalculationType.FIXED;

    /** Points to the component whose value is used as the base for percentage calc. */
    @Column(name = "percentage_of_component_id")
    private UUID percentageOfComponentId;

    @Column(name = "percentage_value", precision = 10, scale = 4)
    private BigDecimal percentageValue;

    @Column(name = "formula_expression", length = 500)
    private String formulaExpression;

    @Column(name = "is_taxable", nullable = false)
    private boolean taxable = true;

    @Column(name = "is_part_of_ctc", nullable = false)
    private boolean partOfCtc = true;

    @Column(name = "is_part_of_gross", nullable = false)
    private boolean partOfGross = true;

    @Column(name = "is_pro_rata", nullable = false)
    private boolean proRata = true;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum ComponentType {
        EARNING, DEDUCTION, REIMBURSEMENT, EMPLOYER_CONTRIBUTION
    }

    public enum CalculationType {
        FIXED, PERCENTAGE, FORMULA
    }
}
