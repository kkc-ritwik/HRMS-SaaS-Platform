package com.hrms.compensation.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "benefits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Benefit extends BaseEntity {

    public enum BenefitType {
        HEALTH, DENTAL, VISION, LIFE, RETIREMENT, OTHER
    }

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "benefit_type", nullable = false, length = 50)
    private BenefitType benefitType = BenefitType.OTHER;

    @Column(name = "provider", length = 200)
    private String provider;

    @Column(name = "coverage_amount", precision = 15, scale = 2)
    private BigDecimal coverageAmount;

    @Column(name = "employee_contribution", precision = 15, scale = 2)
    private BigDecimal employeeContribution;

    @Column(name = "employer_contribution", precision = 15, scale = 2)
    private BigDecimal employerContribution;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "USD";

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
