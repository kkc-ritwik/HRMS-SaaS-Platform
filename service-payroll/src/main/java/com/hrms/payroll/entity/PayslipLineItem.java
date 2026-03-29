package com.hrms.payroll.entity;

import lombok.*;

import java.math.BigDecimal;

/**
 * One line in a payslip's components_json JSONB column.
 * Stored as part of a JSON array — no table of its own.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PayslipLineItem {
    private String     componentId;
    private String     code;
    private String     name;
    /** EARNING, DEDUCTION, REIMBURSEMENT, or EMPLOYER_CONTRIBUTION */
    private String     type;
    private BigDecimal amount;
}
