package com.hrms.onboarding.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "onboarding_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingTask extends BaseEntity {

    public enum TaskType {
        DOCUMENT, TRAINING, MEETING, SYSTEM_ACCESS, OTHER
    }

    public enum TaskStatus {
        PENDING, IN_PROGRESS, COMPLETED, SKIPPED
    }

    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 50)
    @Builder.Default
    private TaskType taskType = TaskType.OTHER;

    @Column(name = "assigned_to_role", length = 100)
    private String assignedToRole;

    @Column(name = "due_days_offset", nullable = false)
    @Builder.Default
    private int dueDaysOffset = 0;

    @Column(name = "required", nullable = false)
    @Builder.Default
    private boolean required = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "completed_at")
    private Instant completedAt;
}
