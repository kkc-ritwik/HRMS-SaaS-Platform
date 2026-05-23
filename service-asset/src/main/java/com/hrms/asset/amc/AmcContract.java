package com.hrms.asset.amc;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Annual Maintenance Contract — covers warranty + post-warranty support for assets.
 * One AMC can cover multiple assets (e.g. a single contract for all 50 laptops at a site).
 */
@Entity
@Table(name = "amc_contracts",
        indexes = @Index(name = "ix_amc_vendor", columnList = "tenant_id,vendor_name"))
@Auditable("AmcContract")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AmcContract extends BaseEntity {

    @Column(name = "contract_number", length = 100, nullable = false) private String contractNumber;
    @Column(name = "name", length = 200, nullable = false) private String name;
    @Column(name = "vendor_name", length = 200, nullable = false) private String vendorName;
    @Column(name = "vendor_contact_email", length = 200) private String vendorContactEmail;
    @Column(name = "vendor_contact_phone", length = 30) private String vendorContactPhone;
    @Column(name = "vendor_account_manager", length = 200) private String vendorAccountManager;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", length = 30, nullable = false)
    private ContractType contractType;

    @Column(name = "scope_description", length = 4000) private String scopeDescription;

    @Column(name = "start_date", nullable = false) private LocalDate startDate;
    @Column(name = "end_date", nullable = false) private LocalDate endDate;
    @Column(name = "auto_renewal") private Boolean autoRenewal;
    @Column(name = "renewal_notice_days") private Integer renewalNoticeDays;

    @Column(name = "annual_value", precision = 14, scale = 2) private BigDecimal annualValue;
    @Column(name = "currency", length = 3) private String currency;
    @Column(name = "payment_terms", length = 200) private String paymentTerms;

    @Column(name = "sla_response_hours") private Integer slaResponseHours;
    @Column(name = "sla_resolution_hours") private Integer slaResolutionHours;
    @Column(name = "support_hours", length = 100) private String supportHours;  // "9x5", "24x7"

    /** Assets covered by this AMC. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "covered_asset_ids", columnDefinition = "jsonb")
    private List<UUID> coveredAssetIds;

    /** Asset categories covered (alternative to listing individual assets). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "covered_category_ids", columnDefinition = "jsonb")
    private List<UUID> coveredCategoryIds;

    @Column(name = "contract_document_uri", length = 1000) private String contractDocumentUri;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.ACTIVE;

    public enum ContractType { AMC, WARRANTY, INSURANCE, SOFTWARE_LICENSE, SUPPORT_SUBSCRIPTION }
    public enum Status { DRAFT, ACTIVE, EXPIRING_SOON, EXPIRED, CANCELLED, RENEWED }
}
