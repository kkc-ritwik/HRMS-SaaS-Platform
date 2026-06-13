package com.hrms.leave.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/** An employee's pick of an optional/restricted holiday for a given year. */
@Entity
@Table(name = "optional_holiday_selections")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OptionalHolidaySelection extends BaseEntity {

    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "holiday_id", nullable = false) private UUID holidayId;
    @Column(name = "year", nullable = false) private int year;
}
