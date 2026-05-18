package com.hrms.payroll.bonus;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "bonus_allocations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cycle_id","employee_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BonusAllocation extends BaseEntity {

    @Column(name = "cycle_id", nullable = false) private UUID cycleId;
    @Column(name = "employee_id", nullable = false) private UUID employeeId;

    @Column(name = "amount", precision = 14, scale = 2, nullable = false) private BigDecimal amount;
    @Column(name = "rating", length = 10) private String rating;
    @Column(name = "justification", length = 1000) private String justification;
    @Column(name = "tds_withheld", precision = 14, scale = 2) private BigDecimal tdsWithheld;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status = Status.PROPOSED;

    @Column(name = "approved_by") private UUID approvedBy;
    @Column(name = "approved_at") private java.time.Instant approvedAt;
    @Column(name = "paid_in_payslip_id") private UUID paidInPayslipId;

    public enum Status { PROPOSED, APPROVED, REJECTED, PAID }
}
