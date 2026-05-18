package com.hrms.performance.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "role_competencies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("RoleCompetency")
@EntityListeners(AuditEntityListener.class)
public class RoleCompetency extends BaseEntity {

    @Column(name = "competency_id", nullable = false)
    private UUID competencyId;

    /** Role UUID from core-hr service. Nullable if mapping is department-wide. */
    @Column(name = "role_id")
    private UUID roleId;

    /** Department UUID from core-hr service. */
    @Column(name = "department_id")
    private UUID departmentId;

    /** Expected proficiency level (1â€“5). */
    @Column(name = "expected_level", nullable = false)
    private int expectedLevel = 3;

    @Column(name = "weightage", nullable = false, precision = 5, scale = 2)
    private BigDecimal weightage = BigDecimal.valueOf(20);
}
