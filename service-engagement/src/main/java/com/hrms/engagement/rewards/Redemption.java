package com.hrms.engagement.rewards;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/** A specific points-for-catalog-item redemption event. */
@Entity
@Table(name = "engagement_redemptions",
        indexes = @Index(name = "ix_redeem_emp", columnList = "tenant_id,employee_id"))
@Auditable(value = "Redemption", redactFields = "shippingAddress,voucherCodeEncrypted")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Redemption extends BaseEntity {

    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "catalog_item_id", nullable = false) private UUID catalogItemId;
    @Column(name = "quantity", nullable = false) private Integer quantity = 1;
    @Column(name = "points_spent", nullable = false) private Integer pointsSpent;
    @Column(name = "shipping_address", length = 1000) private String shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.REQUESTED;

    @Column(name = "voucher_code_encrypted", length = 500)
    @jakarta.persistence.Convert(converter = com.hrms.security.crypto.PiiEncryptedConverter.class)
    private String voucherCodeEncrypted;

    @Column(name = "fulfilled_at") private OffsetDateTime fulfilledAt;
    @Column(name = "cancelled_at") private OffsetDateTime cancelledAt;
    @Column(name = "cancellation_reason", length = 500) private String cancellationReason;

    public enum Status { REQUESTED, APPROVED, FULFILLED, CANCELLED, REFUNDED }
}
