package com.hrms.compensation.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pay_grades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Auditable("PayGrade")
@EntityListeners(AuditEntityListener.class)
public class PayGrade extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "min_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal minSalary;

    @Column(name = "max_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal maxSalary;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "USD";

    @Column(name = "level", nullable = false)
    private int level = 1;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
