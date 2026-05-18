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
@Table(name = "travel_advances")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Auditable("TravelAdvance")
@EntityListeners(AuditEntityListener.class)
public class TravelAdvance {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "trip_id", nullable = false) private UUID tripId;
    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(precision = 14, scale = 2) private BigDecimal amount;
    @Column(length = 3) private String currency;
    @Enumerated(EnumType.STRING) @Column(length = 30) private Status status;
    @Column(name = "requested_at") private LocalDate requestedAt;
    @Column(name = "disbursed_at") private LocalDate disbursedAt;
    @Column(name = "settled_at") private LocalDate settledAt;
    @Column(name = "settled_amount", precision = 14, scale = 2) private BigDecimal settledAmount;

    public enum Status { REQUESTED, APPROVED, DISBURSED, SETTLED, LAPSED }
}
