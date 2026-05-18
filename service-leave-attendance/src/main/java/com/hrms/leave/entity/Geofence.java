package com.hrms.leave.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/** Circular geofence around an office or work site for attendance validation. */
@Entity
@Table(name = "geofences", indexes = @Index(name = "ix_geofence_location", columnList = "location_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("Geofence")
@EntityListeners(AuditEntityListener.class)
public class Geofence extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "location_id")
    private UUID locationId;

    @Column(name = "center_latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal centerLatitude;

    @Column(name = "center_longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal centerLongitude;

    /** Radius in metres within which a punch is considered valid. */
    @Column(name = "radius_meters", nullable = false)
    private Integer radiusMeters;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "applies_to_department_id")
    private UUID appliesToDepartmentId;
}
