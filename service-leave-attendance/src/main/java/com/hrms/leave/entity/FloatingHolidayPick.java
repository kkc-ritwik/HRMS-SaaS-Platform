package com.hrms.leave.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/** Each employee's chosen floating holidays for a given pool. */
@Entity
@Table(name = "floating_holiday_picks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"pool_id","employee_id","holiday_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FloatingHolidayPick extends BaseEntity {

    @Column(name = "pool_id", nullable = false) private UUID poolId;
    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "holiday_id", length = 100, nullable = false) private String holidayId;
    @Column(name = "holiday_date", nullable = false) private LocalDate holidayDate;
    @Column(name = "holiday_name", length = 200) private String holidayName;
}
