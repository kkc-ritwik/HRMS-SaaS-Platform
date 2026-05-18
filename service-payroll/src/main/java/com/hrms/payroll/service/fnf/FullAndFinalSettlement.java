package com.hrms.payroll.service.fnf;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class FullAndFinalSettlement {
    private UUID employeeId;
    private String tenantId;
    private LocalDate joinDate;
    private LocalDate exitDate;
    private LocalDate lastWorkingDate;

    /** Pro-rated salary for the final partial month. */
    private BigDecimal finalMonthSalary;

    /** Leave encashment for unused balance. */
    private BigDecimal leaveEncashment;
    private BigDecimal leaveEncashmentDays;

    /** Statutory gratuity. */
    private BigDecimal gratuity;

    /** Notice-period recovery (deduction, negative impact). */
    private BigDecimal noticePeriodRecovery;

    /** Outstanding loan balance recovered. */
    private BigDecimal loanRecovery;

    /** Outstanding salary advance recovered. */
    private BigDecimal advanceRecovery;

    /** Outstanding expense reimbursements paid out. */
    private BigDecimal reimbursementsPayout;

    /** Bonus / variable pay due. */
    private BigDecimal bonusPayout;

    /** TDS withheld on FnF earnings. */
    private BigDecimal tdsWithheld;

    /** Net amount (positive = pay to employee; negative = recover from employee). */
    private BigDecimal netSettlement;

    /** Per-line breakdown for the FnF letter / report. */
    @Singular("line") private List<FnfLine> lines;

    @Data @Builder
    public static class FnfLine {
        private String label;
        /** "EARNING" or "DEDUCTION" */
        private String type;
        private BigDecimal amount;
        private String note;
    }
}
