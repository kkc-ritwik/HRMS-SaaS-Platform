package com.hrms.timesheet.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "project_tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Auditable("Task")
@EntityListeners(AuditEntityListener.class)
public class Task {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "project_id", nullable = false) private UUID projectId;
    @Column(nullable = false, length = 200) private String name;
    @Column(length = 1000) private String description;
    @Column(name = "is_billable") private Boolean isBillable;
    @Column(name = "is_active") private Boolean isActive;
}
