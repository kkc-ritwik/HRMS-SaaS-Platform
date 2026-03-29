package com.hrms.compliance.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "compliance_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceTask extends BaseEntity {

    public enum TaskStatus {
        PENDING, IN_PROGRESS, COMPLETED, OVERDUE
    }

    @Column(name = "compliance_item_id", nullable = false)
    private UUID complianceItemId;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "assignee_id")
    private UUID assigneeId;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "evidence_url", length = 500)
    private String evidenceUrl;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
