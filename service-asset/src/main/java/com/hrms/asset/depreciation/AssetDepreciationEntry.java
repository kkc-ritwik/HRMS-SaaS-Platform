package com.hrms.asset.depreciation;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/** One monthly depreciation journal entry per asset per period. */
@Entity
@Table(name = "asset_depreciation_entries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","asset_id","period_year","period_month"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AssetDepreciationEntry extends BaseEntity {

    @Column(name = "asset_id", nullable = false) private UUID assetId;
    @Column(name = "period_year", nullable = false) private Integer periodYear;
    @Column(name = "period_month", nullable = false) private Integer periodMonth;
    @Column(name = "depreciation_amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal depreciationAmount;
    @Column(name = "book_value_after", precision = 14, scale = 2)
    private BigDecimal bookValueAfter;
    @Column(name = "method", length = 30) private String method;
    @Column(name = "posted_to_gl") private Boolean postedToGl;
}
