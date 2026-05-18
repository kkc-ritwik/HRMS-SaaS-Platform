package com.hrms.corehr.transfer;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Records a transfer, promotion, demotion, or department change with effective dating.
 * Used as the audit trail + driver of effective-dated changes to the Employee record.
 */
@Entity
@Table(name = "employee_transfers", indexes = {
        @Index(name = "ix_xfer_emp", columnList = "tenant_id,employee_id"),
        @Index(name = "ix_xfer_effective", columnList = "effective_date")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EmployeeTransfer extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false)
    private Type type;

    @Column(name = "from_department_id") private UUID fromDepartmentId;
    @Column(name = "to_department_id")   private UUID toDepartmentId;

    @Column(name = "from_designation_id") private UUID fromDesignationId;
    @Column(name = "to_designation_id")   private UUID toDesignationId;

    @Column(name = "from_location_id") private UUID fromLocationId;
    @Column(name = "to_location_id")   private UUID toLocationId;

    @Column(name = "from_manager_id") private UUID fromManagerId;
    @Column(name = "to_manager_id")   private UUID toManagerId;

    @Column(name = "from_pay_grade_id") private UUID fromPayGradeId;
    @Column(name = "to_pay_grade_id")   private UUID toPayGradeId;

    @Column(name = "old_salary", precision = 14, scale = 2)
    private BigDecimal oldSalary;
    @Column(name = "new_salary", precision = 14, scale = 2)
    private BigDecimal newSalary;
    @Column(name = "salary_change_percentage", precision = 6, scale = 2)
    private BigDecimal salaryChangePercentage;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "applied_at")
    private java.time.Instant appliedAt;

    public enum Type { TRANSFER, PROMOTION, DEMOTION, LATERAL, CONFIRMATION, DEPARTMENT_CHANGE, LOCATION_CHANGE }
    public enum Status { PENDING, APPROVED, APPLIED, REJECTED, CANCELLED }
}
