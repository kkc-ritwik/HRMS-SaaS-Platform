package com.hrms.leave.biometric;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/** A registered biometric / face / RFID attendance device. */
@Entity
@Table(name = "biometric_devices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BiometricDevice extends BaseEntity {

    @Column(name = "device_code", length = 100, nullable = false) private String deviceCode;
    @Column(name = "name", length = 200, nullable = false) private String name;
    @Column(name = "make", length = 100) private String make;
    @Column(name = "model", length = 100) private String model;
    @Column(name = "location_id") private UUID locationId;
    @Column(name = "ip_address", length = 64) private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "last_sync_at") private Instant lastSyncAt;
    @Column(name = "last_seen_at") private Instant lastSeenAt;
    @Column(name = "firmware", length = 50) private String firmware;
    @Column(name = "notes", length = 1000) private String notes;

    public enum Status { ACTIVE, INACTIVE, OFFLINE, MAINTENANCE }
}
