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
import java.util.UUID;

@Entity
@Table(name = "holidays")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("Holiday")
@EntityListeners(AuditEntityListener.class)
public class Holiday extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private HolidayType type = HolidayType.NATIONAL;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "location_ids", columnDefinition = "jsonb")
    private List<UUID> locationIds;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "year", nullable = false)
    private int year;

    public enum HolidayType {
        NATIONAL, RESTRICTED, OPTIONAL
    }
}
