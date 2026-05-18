package com.hrms.timesheet.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "timesheet_entries",
        indexes = {
            @Index(name = "ix_ts_emp_date", columnList = "tenant_id,employee_id,work_date"),
            @Index(name = "ix_ts_project", columnList = "project_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TimesheetEntry {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "project_id", nullable = false) private UUID projectId;
    @Column(name = "task_id") private UUID taskId;
    @Column(name = "work_date", nullable = false) private LocalDate workDate;
    @Column(precision = 5, scale = 2) private BigDecimal hours;
    @Column(name = "is_billable") private Boolean isBillable;
    @Column(length = 1000) private String notes;
    @Enumerated(EnumType.STRING) @Column(length = 30) private Status status;

    public enum Status { DRAFT, SUBMITTED, APPROVED, REJECTED }
}
