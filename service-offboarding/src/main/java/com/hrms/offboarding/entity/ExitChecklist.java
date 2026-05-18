package com.hrms.offboarding.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "exit_checklists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Auditable("ExitChecklist")
@EntityListeners(AuditEntityListener.class)
public class ExitChecklist extends BaseEntity {

    @Column(name = "separation_id", nullable = false)
    private UUID separationId;

    @Column(name = "task_title", nullable = false, length = 300)
    private String taskTitle;

    @Column(name = "task_category", length = 100)
    private String taskCategory;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "notes", length = 500)
    private String notes;
}
