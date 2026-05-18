package com.hrms.travel.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "per_diem_rates",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","country","city","grade"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PerDiemRate {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(length = 100) private String country;
    @Column(length = 100) private String city;
    @Column(length = 50) private String grade;       // links to pay-grade code
    @Column(name = "daily_amount", precision = 14, scale = 2, nullable = false) private BigDecimal dailyAmount;
    @Column(name = "lodging_cap", precision = 14, scale = 2) private BigDecimal lodgingCap;
    @Column(name = "meals_cap", precision = 14, scale = 2) private BigDecimal mealsCap;
    @Column(length = 3) private String currency;
    @Column(name = "is_active") private Boolean isActive;
}
