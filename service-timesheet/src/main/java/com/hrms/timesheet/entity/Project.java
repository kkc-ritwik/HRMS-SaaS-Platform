package com.hrms.timesheet.entity;

import com.hrms.audit.annotation.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "projects", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","code"}))
@Auditable("Project")
@EntityListeners(com.hrms.audit.listener.AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Project {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(nullable = false, length = 200) private String name;
    @Column(nullable = false, length = 50) private String code;
    @Column(length = 200) private String client;
    @Column(name = "start_date") private LocalDate startDate;
    @Column(name = "end_date") private LocalDate endDate;
    @Enumerated(EnumType.STRING) @Column(length = 30) private Status status;
    @Column(name = "billing_rate", precision = 12, scale = 2) private BigDecimal billingRate;
    @Column(length = 3) private String currency;
    @Column(name = "is_billable") private Boolean isBillable;
    @Column(name = "manager_id") private UUID managerId;

    public enum Status { PLANNED, ACTIVE, ON_HOLD, COMPLETED, ARCHIVED }
}
