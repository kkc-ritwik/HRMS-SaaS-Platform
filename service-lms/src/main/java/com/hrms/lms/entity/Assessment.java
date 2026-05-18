package com.hrms.lms.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Auditable("Assessment")
@EntityListeners(AuditEntityListener.class)
public class Assessment extends BaseEntity {

    public enum AssessmentStatus { DRAFT, ACTIVE, INACTIVE }

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_marks", nullable = false)
    private int totalMarks = 100;

    @Column(name = "passing_marks", nullable = false)
    private int passingMarks = 60;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "attempts_allowed", nullable = false)
    private int attemptsAllowed = 3;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AssessmentStatus status = AssessmentStatus.DRAFT;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "questions", columnDefinition = "jsonb")
    private List<String> questions = new ArrayList<>();
}
