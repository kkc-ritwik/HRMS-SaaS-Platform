package com.hrms.payroll.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "esi_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("EsiConfig")
@EntityListeners(AuditEntityListener.class)
public class EsiConfig extends BaseEntity {

    @Column(name = "esi_number", length = 50)
    private String esiNumber;

    /** Gross wage ceiling for ESI applicability (â‚¹21,000 as per Indian law). */
    @Column(name = "wage_ceiling", nullable = false, precision = 10, scale = 2)
    private BigDecimal wageCeiling = BigDecimal.valueOf(21000);

    /** Employee ESI contribution rate (%) â€” default 0.75%. */
    @Column(name = "employee_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal employeeRate = BigDecimal.valueOf(0.75);

    /** Employer ESI contribution rate (%) â€” default 3.25%. */
    @Column(name = "employer_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal employerRate = BigDecimal.valueOf(3.25);

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;
}
