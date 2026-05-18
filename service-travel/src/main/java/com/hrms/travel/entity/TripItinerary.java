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
@Table(name = "trip_itineraries", indexes = @Index(name = "ix_itin_trip", columnList = "trip_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Auditable("TripItinerary")
@EntityListeners(AuditEntityListener.class)
public class TripItinerary {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "trip_id", nullable = false) private UUID tripId;

    @Enumerated(EnumType.STRING) @Column(length = 30) private LegType type; // FLIGHT/HOTEL/TRAIN/CAR/TAXI
    @Column(name = "from_place", length = 200) private String fromPlace;
    @Column(name = "to_place", length = 200) private String toPlace;
    @Column(name = "depart_at") private LocalDate departAt;
    @Column(name = "arrive_at") private LocalDate arriveAt;
    @Column(length = 200) private String vendor;
    @Column(name = "booking_reference", length = 100) private String bookingReference;
    @Column(precision = 14, scale = 2) private BigDecimal cost;
    @Column(length = 3) private String currency;
    @Column(name = "booked_by_company") private Boolean bookedByCompany;

    public enum LegType { FLIGHT, HOTEL, TRAIN, CAR, TAXI, OTHER }
}
