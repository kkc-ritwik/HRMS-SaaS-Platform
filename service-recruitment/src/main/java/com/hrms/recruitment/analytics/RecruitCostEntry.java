package com.hrms.recruitment.analytics;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Recruitment cost ledger entry. Aggregated by date-range to compute cost-per-hire.
 * Sources: agency invoices, job-board ads, sign-on bonuses, referral bonuses,
 * background-verification fees, ATS subscription, recruiter salary allocation.
 */
@Entity
@Table(name = "recruit_cost_entries",
        indexes = @Index(name = "ix_recruit_cost_date", columnList = "tenant_id,incurred_on"))
@Auditable("RecruitCostEntry")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RecruitCostEntry extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30, nullable = false)
    private Category category;

    @Column(name = "amount", precision = 14, scale = 2, nullable = false) private BigDecimal amount;
    @Column(name = "currency", length = 3, nullable = false) private String currency = "INR";

    @Column(name = "incurred_on", nullable = false) private LocalDate incurredOn;

    @Column(name = "requisition_id") private UUID requisitionId;
    @Column(name = "candidate_id") private UUID candidateId;
    @Column(name = "agency_id") private UUID agencyId;
    @Column(name = "referrer_employee_id") private UUID referrerEmployeeId;

    @Column(name = "description", length = 500) private String description;
    @Column(name = "invoice_reference", length = 100) private String invoiceReference;

    public enum Category {
        AGENCY_FEE, JOB_BOARD, REFERRAL_BONUS, SIGN_ON_BONUS, BACKGROUND_CHECK,
        ATS_SUBSCRIPTION, RECRUITER_TIME, TRAVEL, ASSESSMENT_TOOLS, OTHER
    }
}
