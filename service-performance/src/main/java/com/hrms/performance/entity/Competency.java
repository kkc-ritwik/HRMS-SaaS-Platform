package com.hrms.performance.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "competencies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("Competency")
@EntityListeners(AuditEntityListener.class)
public class Competency extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private CompetencyCategory category = CompetencyCategory.CORE;

    /**
     * Behavior descriptions indexed by proficiency level 1â€“5.
     * Index 0 = level 1, index 4 = level 5.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "behaviors", columnDefinition = "jsonb")
    private List<String> behaviors;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // â”€â”€ Enum â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public enum CompetencyCategory { CORE, FUNCTIONAL, LEADERSHIP, BEHAVIORAL }
}
