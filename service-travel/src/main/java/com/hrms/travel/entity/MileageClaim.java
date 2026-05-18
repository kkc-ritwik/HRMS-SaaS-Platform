package com.hrms.travel.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "mileage_claims", indexes = @Index(name = "ix_mileage_emp_date", columnList = "tenant_id,employee_id,travel_date"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Auditable("MileageClaim")
@EntityListeners(AuditEntityListener.class)
public class MileageClaim {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "trip_id") private UUID tripId;

    @Column(name = "travel_date", nullable = false) private LocalDate travelDate;
    @Column(name = "from_location", length = 200) private String fromLocation;
    @Column(name = "to_location", length = 200) private String toLocation;
    @Column(precision = 10, scale = 2) private BigDecimal kilometers;
    @Column(name = "rate_per_km", precision = 8, scale = 2) private BigDecimal ratePerKm;
    @Column(precision = 14, scale = 2) private BigDecimal amount;
    @Column(length = 3) private String currency;
    @Enumerated(EnumType.STRING) @Column(length = 30) private Status status;
    @Column(length = 500) private String remarks;

    public enum Status { DRAFT, SUBMITTED, APPROVED, REJECTED, PAID }
}
