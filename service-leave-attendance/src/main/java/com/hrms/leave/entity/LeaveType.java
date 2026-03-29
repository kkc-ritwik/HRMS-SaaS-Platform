package com.hrms.leave.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "leave_types")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LeaveType extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "is_paid", nullable = false)
    private boolean paid = true;

    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "applies_to_gender", length = 20)
    private String appliesToGender;

    @Column(name = "applies_to_employment_type", length = 30)
    private String appliesToEmploymentType;

    @Column(name = "max_days_per_year", precision = 6, scale = 2)
    private BigDecimal maxDaysPerYear;

    @Column(name = "requires_attachment_after_days", precision = 6, scale = 2)
    private BigDecimal requiresAttachmentAfterDays;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
