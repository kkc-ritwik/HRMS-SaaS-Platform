package com.hrms.lms.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "course_modules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Auditable("CourseModule")
@EntityListeners(AuditEntityListener.class)
public class CourseModule extends BaseEntity {

    public enum ContentType { VIDEO, DOCUMENT, QUIZ, SCORM, LINK }
    public enum ModuleStatus { DRAFT, PUBLISHED }

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "order_index", nullable = false)
    private int orderIndex = 0;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 30)
    private ContentType contentType = ContentType.DOCUMENT;

    @Column(name = "content_url", length = 500)
    private String contentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ModuleStatus status = ModuleStatus.DRAFT;
}
