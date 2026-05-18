package com.hrms.compensation.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employee_benefits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Auditable("EmployeeBenefit")
@EntityListeners(AuditEntityListener.class)
public class EmployeeBenefit extends BaseEntity {

    public enum BenefitStatus {
        ACTIVE, TERMINATED, PENDING
    }

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "benefit_id", nullable = false)
    private UUID benefitId;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BenefitStatus status = BenefitStatus.ACTIVE;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
