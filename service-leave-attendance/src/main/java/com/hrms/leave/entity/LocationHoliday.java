package com.hrms.leave.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Holiday entry scoped to a location (state / country / office). Used in conjunction with
 * the global `holidays` table â€” location-specific entries override or augment globals.
 */
@Entity
@Table(name = "location_holidays",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","location_id","holiday_date"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LocationHoliday extends BaseEntity {

    @Column(name = "location_id", nullable = false)
    private UUID locationId;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "is_optional")
    private Boolean isOptional;

    @Column(name = "description", length = 500)
    private String description;
}
