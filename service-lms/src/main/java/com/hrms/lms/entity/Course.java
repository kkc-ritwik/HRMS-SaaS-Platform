package com.hrms.lms.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Auditable("Course")
@EntityListeners(AuditEntityListener.class)
public class Course extends BaseEntity {

    public enum CourseLevel { BEGINNER, INTERMEDIATE, ADVANCED }
    public enum CourseFormat { ONLINE, CLASSROOM, BLENDED, SELF_PACED }
    public enum CourseStatus { DRAFT, PUBLISHED, ARCHIVED }

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "instructor_id")
    private UUID instructorId;

    @Column(name = "duration_hours", precision = 6, scale = 2)
    private BigDecimal durationHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 20)
    private CourseLevel level = CourseLevel.BEGINNER;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 20)
    private CourseFormat format = CourseFormat.ONLINE;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CourseStatus status = CourseStatus.DRAFT;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    private List<String> tags = new ArrayList<>();

    @Column(name = "mandatory", nullable = false)
    private boolean mandatory = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_roles", columnDefinition = "jsonb")
    private List<String> targetRoles = new ArrayList<>();
}
