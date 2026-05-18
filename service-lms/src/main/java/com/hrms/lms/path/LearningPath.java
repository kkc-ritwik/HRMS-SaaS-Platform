package com.hrms.lms.path;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

/**
 * A learning path bundles courses into a sequenced curriculum (e.g. "New Manager Path").
 * Steps are ordered; some may require completion of earlier steps as prerequisites.
 */
@Entity
@Table(name = "lms_learning_paths")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("LearningPath")
@EntityListeners(AuditEntityListener.class)
public class LearningPath extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200) private String name;
    @Column(name = "description", length = 2000) private String description;
    @Column(name = "category", length = 100) private String category;
    @Column(name = "is_mandatory") private Boolean isMandatory;

    /** Optional audience filter â€” applies to all if null. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "audience_filter", columnDefinition = "jsonb")
    private java.util.Map<String, Object> audienceFilter;

    /** Ordered list of { courseId, displayOrder, prerequisiteIds } objects. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "steps", columnDefinition = "jsonb")
    private List<PathStep> steps;

    @Column(name = "is_active", nullable = false) private boolean active = true;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class PathStep {
        private UUID courseId;
        private Integer displayOrder;
        private List<UUID> prerequisiteCourseIds;
        private Boolean optional;
    }
}
