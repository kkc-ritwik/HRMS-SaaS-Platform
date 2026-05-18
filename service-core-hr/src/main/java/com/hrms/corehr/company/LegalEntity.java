package com.hrms.corehr.company;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * A legal entity (company / subsidiary / branch). A tenant can have multiple legal
 * entities â€” payroll, statutory filings (PF, ESI, TDS), and bank disbursement are all
 * scoped per legal entity. Employee.legal_entity_id points here.
 *
 * Supports parent-child hierarchy (parent_id = null for the top org).
 */
@Entity
@Table(name = "legal_entities",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","code"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LegalEntity extends BaseEntity {

    @Column(name = "code", length = 50, nullable = false) private String code;
    @Column(name = "name", length = 200, nullable = false) private String name;
    @Column(name = "registered_name", length = 300) private String registeredName;

    @Column(name = "parent_id") private UUID parentId;

    @Column(name = "country", length = 2, nullable = false) private String country;
    @Column(name = "base_currency", length = 3, nullable = false) private String baseCurrency;
    @Column(name = "time_zone", length = 50) private String timeZone;

    @Column(name = "registered_address", length = 1000) private String registeredAddress;
    @Column(name = "city", length = 100) private String city;
    @Column(name = "state", length = 100) private String state;
    @Column(name = "postal_code", length = 20) private String postalCode;

    // Statutory identifiers
    @Column(name = "tax_id", length = 50) private String taxId;        // GSTIN / VAT / TIN
    @Column(name = "tan_number", length = 20) private String tanNumber;
    @Column(name = "pan_number", length = 20) private String panNumber;
    @Column(name = "cin_number", length = 30) private String cinNumber;
    @Column(name = "epfo_establishment_id", length = 30) private String epfoEstablishmentId;
    @Column(name = "esic_employer_code", length = 30) private String esicEmployerCode;
    @Column(name = "lwf_registration_number", length = 50) private String lwfRegistrationNumber;

    // Payroll / financial settings
    @Column(name = "default_pay_frequency", length = 20) private String defaultPayFrequency; // MONTHLY / BI_WEEKLY / WEEKLY
    @Column(name = "fiscal_year_start_month") private Integer fiscalYearStartMonth;
    @Column(name = "bank_account_for_disbursement", length = 40) private String bankAccountForDisbursement;
    @Column(name = "bank_ifsc", length = 20) private String bankIfsc;

    @Column(name = "logo_url", length = 1000) private String logoUrl;
    @Column(name = "letterhead_url", length = 1000) private String letterheadUrl;

    @Column(name = "is_active", nullable = false) private boolean active = true;
}
