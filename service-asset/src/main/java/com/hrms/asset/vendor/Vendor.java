package com.hrms.asset.vendor;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Vendor master — any external party HR / Finance pays: asset suppliers, AMC providers,
 * recruitment agencies, BGV agencies, training partners, insurance brokers, catering, etc.
 * Tracks GSTIN, PAN, MSME status, bank details (encrypted), preferred payment terms,
 * and ratings for governance.
 *
 * Used by service-asset (AMC contracts), service-recruitment (agency invoices),
 * service-expense (vendor bills), service-compliance (TDS Form 26Q).
 */
@Entity
@Table(name = "vendors", indexes = {
        @Index(name = "ix_vendor_active", columnList = "tenant_id,active"),
        @Index(name = "ix_vendor_category", columnList = "tenant_id,category")
})
@Auditable(value = "Vendor", redactFields = "panNumber,gstin,bankAccount,ifsc")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Vendor extends BaseEntity {

    @Column(name = "vendor_code", length = 50, nullable = false) private String vendorCode;
    @Column(name = "legal_name", length = 300, nullable = false) private String legalName;
    @Column(name = "display_name", length = 200) private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30, nullable = false)
    private Category category;

    @Column(name = "primary_contact_name", length = 200) private String primaryContactName;
    @Column(name = "primary_contact_email", length = 200) private String primaryContactEmail;
    @Column(name = "primary_contact_phone", length = 30) private String primaryContactPhone;

    @Column(name = "address_line1", length = 300) private String addressLine1;
    @Column(name = "address_line2", length = 300) private String addressLine2;
    @Column(name = "city", length = 100) private String city;
    @Column(name = "state", length = 100) private String state;
    @Column(name = "postal_code", length = 20) private String postalCode;
    @Column(name = "country", length = 2) private String country;

    @Column(name = "pan_number", length = 200)
    @jakarta.persistence.Convert(converter = com.hrms.security.crypto.PiiEncryptedConverter.class)
    private String panNumber;

    @Column(name = "gstin", length = 200)
    @jakarta.persistence.Convert(converter = com.hrms.security.crypto.PiiEncryptedConverter.class)
    private String gstin;

    @Column(name = "tan_number", length = 30) private String tanNumber;
    @Column(name = "msme_registered") private Boolean msmeRegistered;
    @Column(name = "msme_classification", length = 20) private String msmeClassification; // MICRO/SMALL/MEDIUM
    @Column(name = "udyam_number", length = 30) private String udyamNumber;

    @Column(name = "bank_name", length = 200) private String bankName;
    @Column(name = "bank_account", length = 200)
    @jakarta.persistence.Convert(converter = com.hrms.security.crypto.PiiEncryptedConverter.class)
    private String bankAccount;
    @Column(name = "ifsc", length = 200)
    @jakarta.persistence.Convert(converter = com.hrms.security.crypto.PiiEncryptedConverter.class)
    private String ifsc;
    @Column(name = "swift_code", length = 30) private String swiftCode;

    @Column(name = "payment_terms_days") private Integer paymentTermsDays;
    @Column(name = "credit_limit", precision = 14, scale = 2) private BigDecimal creditLimit;
    @Column(name = "currency", length = 3) private String currency;

    @Column(name = "tds_section", length = 20) private String tdsSection;
    @Column(name = "tds_rate_percent", precision = 5, scale = 2) private BigDecimal tdsRatePercent;

    @Column(name = "onboarded_on") private LocalDate onboardedOn;
    @Column(name = "contract_start") private LocalDate contractStart;
    @Column(name = "contract_end") private LocalDate contractEnd;

    @Column(name = "rating") private Integer rating;            // 1-5 internal performance rating
    @Column(name = "blacklisted") private Boolean blacklisted;
    @Column(name = "blacklist_reason", length = 1000) private String blacklistReason;

    @Column(name = "active") private Boolean active = true;

    public enum Category {
        ASSET_SUPPLIER, AMC_PROVIDER, RECRUITMENT_AGENCY, BGV_AGENCY, TRAINING_PARTNER,
        INSURANCE_BROKER, CATERING, HOUSEKEEPING, SECURITY, IT_VENDOR, CONSULTANT,
        LEGAL, AUDITOR, TRAVEL_AGENCY, FREIGHT, MARKETING, MEDICAL, OTHER
    }
}
