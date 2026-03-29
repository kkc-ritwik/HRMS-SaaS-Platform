package com.hrms.corehr.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employee_lifecycle_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EmployeeLifecycleEvent extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "approved_by")
    private UUID approvedBy;

    public enum EventType {
        JOINED, PROMOTED, TRANSFERRED, STATUS_CHANGED, RESIGNED, TERMINATED
    }
}
