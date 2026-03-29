package com.hrms.expense.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "expense_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpensePolicy extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "employee_level", length = 100)
    private String employeeLevel;

    @Column(name = "max_amount", precision = 15, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "USD";

    @Column(name = "approval_required", nullable = false)
    private boolean approvalRequired = true;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
