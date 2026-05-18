package com.hrms.travel.entity;

import com.hrms.audit.annotation.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "trip_requests", indexes = {
        @Index(name = "ix_trip_emp", columnList = "tenant_id,employee_id"),
        @Index(name = "ix_trip_status", columnList = "tenant_id,status")
})
@Auditable("Trip")
@EntityListeners(com.hrms.audit.listener.AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TripRequest {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "employee_id", nullable = false) private UUID employeeId;

    @Column(nullable = false, length = 200) private String purpose;
    @Enumerated(EnumType.STRING) @Column(length = 30) private TripType tripType; // DOMESTIC / INTERNATIONAL

    @Column(name = "from_location", length = 200) private String fromLocation;
    @Column(name = "to_location", length = 200) private String toLocation;
    @Column(name = "departure_date") private LocalDate departureDate;
    @Column(name = "return_date") private LocalDate returnDate;

    @Column(name = "estimated_cost", precision = 14, scale = 2) private BigDecimal estimatedCost;
    @Column(length = 3) private String currency;

    @Enumerated(EnumType.STRING) @Column(length = 30) private TripStatus status; // DRAFT/SUBMITTED/APPROVED/REJECTED/CANCELLED/COMPLETED
    @Column(name = "approver_id") private UUID approverId;
    @Column(name = "approved_at") private OffsetDateTime approvedAt;
    @Column(name = "rejection_reason", length = 1000) private String rejectionReason;

    @Column(name = "client_billable") private Boolean clientBillable;
    @Column(name = "project_code", length = 50) private String projectCode;

    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;

    @PrePersist void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
        if (status == null) status = TripStatus.DRAFT;
    }
    @PreUpdate void preUpdate() { updatedAt = OffsetDateTime.now(); }

    public enum TripType { DOMESTIC, INTERNATIONAL, LOCAL }
    public enum TripStatus { DRAFT, SUBMITTED, APPROVED, REJECTED, CANCELLED, COMPLETED }
}
