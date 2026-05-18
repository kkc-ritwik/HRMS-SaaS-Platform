package com.hrms.leave.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "shift_schedules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("ShiftSchedule")
@EntityListeners(AuditEntityListener.class)
public class ShiftSchedule extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "shift_id")
    private UUID shiftId;

    @Column(name = "date", nullable = false)
    private LocalDate scheduleDate;

    @Column(name = "is_week_off", nullable = false)
    private boolean weekOff = false;

    @Column(name = "is_holiday", nullable = false)
    private boolean holiday = false;

    @Column(name = "assigned_by", length = 100)
    private String assignedBy;
}
