package com.hrms.payroll.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "salary_structures")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SalaryStructure extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_default", nullable = false)
    private boolean defaultStructure = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
