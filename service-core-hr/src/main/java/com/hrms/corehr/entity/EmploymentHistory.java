package com.hrms.corehr.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employment_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EmploymentHistory extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "designation", length = 150)
    private String designation;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "responsibilities", columnDefinition = "TEXT")
    private String responsibilities;

    @Column(name = "reason_for_leaving", length = 255)
    private String reasonForLeaving;

    @Column(name = "ctc", precision = 12, scale = 2)
    private BigDecimal ctc;
}
