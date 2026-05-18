package com.hrms.compensation.increment;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Annual / mid-year increment cycle. Defines the budget, eligibility, and
 * rating-to-hike mapping. Individual employee proposals live in IncrementProposal.
 */
@Entity
@Table(name = "increment_cycles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("IncrementCycle")
@EntityListeners(AuditEntityListener.class)
public class IncrementCycle extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200) private String name;
    @Column(name = "fiscal_year", nullable = false, length = 9) private String fiscalYear;

    @Column(name = "effective_date", nullable = false) private LocalDate effectiveDate;
    @Column(name = "manager_proposal_deadline") private LocalDate managerProposalDeadline;
    @Column(name = "hr_approval_deadline") private LocalDate hrApprovalDeadline;

    /** Total budget as % of current payroll. e.g. 8.5 = 8.5% bump on aggregate base. */
    @Column(name = "budget_percent_of_payroll", precision = 5, scale = 2)
    private BigDecimal budgetPercentOfPayroll;

    /** { rating -> default-hike-percent }, e.g. {"5": 15, "4": 10, "3": 6, "2": 2, "1": 0}. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rating_to_hike_map", columnDefinition = "jsonb")
    private Map<String, BigDecimal> ratingToHikeMap;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status = Status.DRAFT;

    public enum Status { DRAFT, OPEN_FOR_PROPOSALS, IN_REVIEW, APPROVED, PUBLISHED, CANCELLED }
}
