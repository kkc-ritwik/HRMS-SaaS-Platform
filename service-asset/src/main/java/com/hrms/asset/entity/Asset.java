package com.hrms.asset.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "assets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Asset extends BaseEntity {

    public enum AssetStatus {
        AVAILABLE, ASSIGNED, UNDER_MAINTENANCE, DISPOSED
    }

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "make", length = 100)
    private String make;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_price", precision = 15, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "current_value", precision = 15, scale = 2)
    private BigDecimal currentValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AssetStatus status = AssetStatus.AVAILABLE;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "warranty_expiry")
    private LocalDate warrantyExpiry;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
