package com.hrms.asset.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "asset_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("AssetRequest")
@EntityListeners(AuditEntityListener.class)
public class AssetRequest extends BaseEntity {

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED, FULFILLED, CANCELLED
    }

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "asset_id")
    private UUID assetId;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "required_from")
    private LocalDate requiredFrom;

    @Column(name = "required_until")
    private LocalDate requiredUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
