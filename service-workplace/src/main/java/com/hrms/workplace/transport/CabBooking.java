package com.hrms.workplace.transport;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Employee cab / transport booking. Two modes:
 *   - Office shuttle slot (pre-booked route + pickup point)
 *   - Ad-hoc cab (for late-night work, client visit, airport drop)
 */
@Entity
@Table(name = "cab_bookings",
        indexes = @Index(name = "ix_cab_emp_date", columnList = "tenant_id,employee_id,pickup_at"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CabBooking extends BaseEntity {

    @Column(name = "employee_id", nullable = false) private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_type", length = 30, nullable = false)
    private BookingType bookingType;

    @Column(name = "pickup_at", nullable = false) private Instant pickupAt;
    @Column(name = "pickup_address", length = 500) private String pickupAddress;
    @Column(name = "drop_address", length = 500) private String dropAddress;
    @Column(name = "drop_latitude") private Double dropLatitude;
    @Column(name = "drop_longitude") private Double dropLongitude;
    @Column(name = "estimated_distance_km") private Double estimatedDistanceKm;
    @Column(name = "estimated_cost", precision = 10, scale = 2) private BigDecimal estimatedCost;
    @Column(name = "currency", length = 3) private String currency;

    @Column(name = "vendor_name", length = 100) private String vendorName;
    @Column(name = "vendor_booking_id", length = 100) private String vendorBookingId;
    @Column(name = "driver_name", length = 100) private String driverName;
    @Column(name = "driver_phone", length = 30) private String driverPhone;
    @Column(name = "vehicle_number", length = 20) private String vehicleNumber;

    @Column(name = "reason", length = 500) private String reason;
    @Column(name = "is_after_hours") private Boolean afterHours;
    @Column(name = "needs_security_escort") private Boolean needsSecurityEscort;

    @Column(name = "actual_pickup_at") private Instant actualPickupAt;
    @Column(name = "actual_drop_at") private Instant actualDropAt;
    @Column(name = "actual_cost", precision = 10, scale = 2) private BigDecimal actualCost;
    @Column(name = "trip_rating") private Integer tripRating;
    @Column(name = "trip_feedback", length = 2000) private String tripFeedback;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.REQUESTED;

    public enum BookingType { SHUTTLE_SLOT, AD_HOC, AIRPORT, CLIENT_VISIT, LATE_NIGHT }
    public enum Status { REQUESTED, APPROVED, ASSIGNED, ON_TRIP, COMPLETED, CANCELLED, NO_SHOW }
}
