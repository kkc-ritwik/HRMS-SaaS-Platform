package com.hrms.performance.template;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A reusable review form definition. Each ReviewCycle can pick a template. Sections
 * are weighted; questions can be rating / text / competency-link / goal-link.
 */
@Entity
@Table(name = "perf_review_templates",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","code"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReviewTemplate extends BaseEntity {

    @Column(name = "code", length = 100, nullable = false) private String code;
    @Column(name = "name", length = 200, nullable = false) private String name;
    @Column(name = "description", length = 1000) private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", length = 30, nullable = false)
    private ReviewType reviewType;

    @Column(name = "rating_scale", nullable = false) private Integer ratingScale = 5;

    /** Sections array â€” each { name, weightage, questions:[{id,text,type,required,scaleMin,scaleMax,competencyId,goalLinked}] } */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sections", columnDefinition = "jsonb", nullable = false)
    private List<Map<String, Object>> sections;

    @Column(name = "include_goals") private Boolean includeGoals;
    @Column(name = "include_competencies") private Boolean includeCompetencies;
    @Column(name = "include_360_feedback") private Boolean include360Feedback;

    @Column(name = "is_active", nullable = false) private boolean active = true;

    /** Applicable department(s) â€” null means all. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applies_to_department_ids", columnDefinition = "jsonb")
    private List<UUID> appliesToDepartmentIds;

    public enum ReviewType { SELF, MANAGER, PEER, SKIP_LEVEL, UPWARD, ALL }
}
