package com.hrms.leave.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "attendance_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("AttendanceRecord")
@EntityListeners(AuditEntityListener.class)
public class AttendanceRecord extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "shift_id")
    private UUID shiftId;

    @Column(name = "first_check_in")
    private Instant firstCheckIn;

    @Column(name = "last_check_out")
    private Instant lastCheckOut;

    @Column(name = "total_hours", precision = 5, scale = 2)
    private BigDecimal totalHours;

    @Column(name = "effective_hours", precision = 5, scale = 2)
    private BigDecimal effectiveHours;

    @Column(name = "break_hours", precision = 5, scale = 2)
    private BigDecimal breakHours;

    @Column(name = "overtime_hours", precision = 5, scale = 2)
    private BigDecimal overtimeHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AttendanceStatus status = AttendanceStatus.ABSENT;

    @Column(name = "late_by_mins", nullable = false)
    private int lateByMins = 0;

    @Column(name = "early_leaving_mins", nullable = false)
    private int earlyLeavingMins = 0;

    @Column(name = "source", length = 20)
    private String source;

    @Column(name = "check_in_lat", precision = 10, scale = 7)
    private BigDecimal checkInLat;

    @Column(name = "check_in_lng", precision = 10, scale = 7)
    private BigDecimal checkInLng;

    @Column(name = "is_regularized", nullable = false)
    private boolean regularized = false;

    // â”€â”€ Enum â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public enum AttendanceStatus {
        PRESENT, ABSENT, HALF_DAY, ON_LEAVE, HOLIDAY, WEEK_OFF
    }
}
