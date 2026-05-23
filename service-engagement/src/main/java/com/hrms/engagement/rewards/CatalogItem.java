package com.hrms.engagement.rewards;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Redemption catalog — Amazon vouchers, restaurant cards, extra-leave-day, branded swag,
 * charity donation. Employees spend earned points (from Kudos / Awards) on these.
 */
@Entity
@Table(name = "engagement_catalog_items",
        indexes = @Index(name = "ix_cat_item_active", columnList = "tenant_id,is_active"))
@Auditable("CatalogItem")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CatalogItem extends BaseEntity {

    @Column(name = "sku", length = 50, nullable = false) private String sku;
    @Column(name = "title", length = 200, nullable = false) private String title;
    @Column(name = "description", length = 2000) private String description;
    @Column(name = "image_url", length = 500) private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30, nullable = false)
    private Category category;

    @Column(name = "points_cost", nullable = false) private Integer pointsCost;
    @Column(name = "monetary_value", precision = 12, scale = 2) private BigDecimal monetaryValue;
    @Column(name = "currency", length = 3) private String currency;

    @Column(name = "stock_qty") private Integer stockQty;
    @Column(name = "vendor_name", length = 200) private String vendorName;
    @Column(name = "vendor_sku", length = 100) private String vendorSku;

    @Column(name = "is_active") private Boolean active = true;
    @Column(name = "requires_shipping") private Boolean requiresShipping;

    public enum Category { VOUCHER, MERCHANDISE, EXPERIENCE, EXTRA_LEAVE, CHARITY, GIFT_CARD, SUBSCRIPTION }
}
