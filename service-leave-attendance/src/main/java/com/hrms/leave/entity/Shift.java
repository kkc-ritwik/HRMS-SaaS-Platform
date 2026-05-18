package com.hrms.leave.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "shifts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("Shift")
@EntityListeners(AuditEntityListener.class)
public class Shift extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "break_duration_mins", nullable = false)
    private int breakDurationMins = 0;

    @Column(name = "min_hours_full_day", nullable = false, precision = 5, scale = 2)
    private BigDecimal minHoursFullDay = BigDecimal.valueOf(8.0);

    @Column(name = "min_hours_half_day", nullable = false, precision = 5, scale = 2)
    private BigDecimal minHoursHalfDay = BigDecimal.valueOf(4.0);

    @Column(name = "grace_period_mins", nullable = false)
    private int gracePeriodMins = 0;

    @Column(name = "overtime_threshold_mins", nullable = false)
    private int overtimeThresholdMins = 0;

    @Column(name = "is_flexible", nullable = false)
    private boolean flexible = false;

    @Column(name = "flex_start_time")
    private LocalTime flexStartTime;

    @Column(name = "flex_end_time")
    private LocalTime flexEndTime;

    @Column(name = "core_start_time")
    private LocalTime coreStartTime;

    @Column(name = "core_end_time")
    private LocalTime coreEndTime;

    @Column(name = "is_night_shift", nullable = false)
    private boolean nightShift = false;

    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
