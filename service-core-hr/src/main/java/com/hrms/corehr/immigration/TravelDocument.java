package com.hrms.corehr.immigration;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Employee passport, visa, work permit, OCI/PIO card, etc. Tracked for expiry alerts
 * + compliance audit. Most multinational orgs need this for travel + statutory reporting.
 */
@Entity
@Table(name = "travel_documents",
        indexes = {
            @Index(name = "ix_traveldoc_employee", columnList = "tenant_id,employee_id"),
            @Index(name = "ix_traveldoc_expiry",   columnList = "expiry_date")
        })
@Auditable(value = "TravelDocument", redactFields = "documentNumber")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TravelDocument extends BaseEntity {

    @Column(name = "employee_id", nullable = false) private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 30, nullable = false)
    private DocumentType documentType;

    @Column(name = "document_number", length = 100, nullable = false)
    @jakarta.persistence.Convert(converter = com.hrms.security.crypto.PiiEncryptedConverter.class)
    private String documentNumber;

    @Column(name = "issuing_country", length = 2, nullable = false) private String issuingCountry;
    @Column(name = "issuing_authority", length = 200) private String issuingAuthority;

    @Column(name = "issue_date") private LocalDate issueDate;
    @Column(name = "expiry_date", nullable = false) private LocalDate expiryDate;

    /** For visas: which country it grants entry to. */
    @Column(name = "destination_country", length = 2) private String destinationCountry;
    @Column(name = "visa_category", length = 50) private String visaCategory;   // e.g. H1-B, B1/B2, L1, Schengen
    @Column(name = "entries_allowed", length = 20) private String entriesAllowed; // single/multiple

    @Column(name = "stored_copy_uri", length = 1000) private String storedCopyUri;

    @Column(name = "is_active", nullable = false) private boolean active = true;

    public enum DocumentType { PASSPORT, VISA, WORK_PERMIT, OCI_CARD, PIO_CARD, NATIONAL_ID, DRIVING_LICENSE }
}
