package com.hrms.leave.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attendance_punches")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AttendancePunch extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "punch_time", nullable = false)
    private Instant punchTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 15)
    private PunchType type;

    @Column(name = "source", length = 20)
    private String source;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "is_valid", nullable = false)
    private boolean valid = true;

    public enum PunchType {
        IN, OUT, BREAK_START, BREAK_END
    }
}
