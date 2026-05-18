package com.hrms.leave.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;

/**
 * Pool of optional/floating holidays from which an employee picks N per year.
 * Common pattern: org publishes a list of 10 cultural/religious holidays, employee
 * chooses any 3 per year. Tracked separately from mandatory holidays.
 */
@Entity
@Table(name = "floating_holiday_pools")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("FloatingHolidayPool")
@EntityListeners(AuditEntityListener.class)
public class FloatingHolidayPool extends BaseEntity {

    @Column(name = "name", length = 200, nullable = false) private String name;
    @Column(name = "year", nullable = false) private Integer year;
    @Column(name = "max_picks_per_employee", nullable = false) private Integer maxPicksPerEmployee;
    @Column(name = "applies_to_location_id") private java.util.UUID appliesToLocationId;
    @Column(name = "is_active", nullable = false) private boolean active = true;

    /** Each item: { id, date, name, region, isReligious } */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "available_holidays", columnDefinition = "jsonb")
    private List<Holiday> availableHolidays;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Holiday {
        private String id;
        private LocalDate date;
        private String name;
        private String region;
        private Boolean religious;
    }
}
