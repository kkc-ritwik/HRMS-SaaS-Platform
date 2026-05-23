package com.hrms.payroll.entity;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Backing document the employee uploads to prove a {@link TaxDeclaration} entry.
 * One declaration row can have many proofs (e.g. LIC + PPF + ELSS all under 80C).
 *
 * Workflow:
 *   UPLOADED → UNDER_REVIEW → ACCEPTED | REJECTED | NEEDS_MORE_INFO
 *
 * Finance reviewer's comment + verifiedBy/verifiedAt are captured for audit. Storage
 * URI points to the encrypted object in MinIO/S3.
 */
@Entity
@Table(name = "tax_investment_proofs", indexes = {
        @Index(name = "ix_proof_emp_fy", columnList = "tenant_id,employee_id,financial_year"),
        @Index(name = "ix_proof_status", columnList = "tenant_id,status")
})
@Auditable(value = "InvestmentProof", redactFields = "panNumber,policyNumber,bankAccount")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InvestmentProof extends BaseEntity {

    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "declaration_id") private UUID declarationId;
    @Column(name = "financial_year", length = 10, nullable = false) private String financialYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "section", length = 20, nullable = false)
    private Section section;

    @Column(name = "sub_section", length = 50) private String subSection;
    @Column(name = "instrument_type", length = 100) private String instrumentType;   // LIC / PPF / ELSS / NPS / HOME_LOAN_INT / etc.

    @Column(name = "claimed_amount", precision = 12, scale = 2, nullable = false) private BigDecimal claimedAmount;
    @Column(name = "verified_amount", precision = 12, scale = 2) private BigDecimal verifiedAmount;

    @Column(name = "document_uri", length = 1000, nullable = false) private String documentUri;
    @Column(name = "document_type", length = 100) private String documentType;
    @Column(name = "document_date") private LocalDate documentDate;

    @Column(name = "policy_number", length = 200)
    @jakarta.persistence.Convert(converter = com.hrms.security.crypto.PiiEncryptedConverter.class)
    private String policyNumber;

    @Column(name = "pan_number", length = 200)
    @jakarta.persistence.Convert(converter = com.hrms.security.crypto.PiiEncryptedConverter.class)
    private String panNumber;

    @Column(name = "bank_account", length = 200)
    @jakarta.persistence.Convert(converter = com.hrms.security.crypto.PiiEncryptedConverter.class)
    private String bankAccount;

    @Column(name = "notes", length = 2000) private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.UPLOADED;

    @Column(name = "verified_by") private UUID verifiedBy;
    @Column(name = "verified_at") private OffsetDateTime verifiedAt;
    @Column(name = "reviewer_comment", length = 2000) private String reviewerComment;

    public enum Section {
        SEC_80C, SEC_80CCC, SEC_80CCD_1B, SEC_80D, SEC_80DD, SEC_80E, SEC_80EE,
        SEC_80G, SEC_80GG, SEC_80GGA, SEC_80TTA, SEC_80TTB, SEC_80U,
        HRA_RENT, LTA, SEC_24B_HOME_LOAN_INT, PRINCIPAL_REPAYMENT,
        STANDARD_DEDUCTION, PREV_EMPLOYER_INCOME, OTHER
    }
    public enum Status { UPLOADED, UNDER_REVIEW, ACCEPTED, REJECTED, NEEDS_MORE_INFO }
}
