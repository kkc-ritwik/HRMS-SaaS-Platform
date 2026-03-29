package com.hrms.compensation.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "compensation_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompensationPlan extends BaseEntity {

    public enum PlanStatus {
        DRAFT, ACTIVE, SUPERSEDED
    }

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "pay_grade_id")
    private UUID payGradeId;

    @Column(name = "base_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "USD";

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "allowances", precision = 15, scale = 2)
    private BigDecimal allowances = BigDecimal.ZERO;

    @Column(name = "bonus_percentage", precision = 5, scale = 2)
    private BigDecimal bonusPercentage = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PlanStatus status = PlanStatus.DRAFT;
}
