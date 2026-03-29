package com.hrms.payroll.dto;

import com.hrms.payroll.entity.PayslipLineItem;
import com.hrms.payroll.entity.Payslip;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class PayslipDto {

    @Getter @Setter @Builder
    public static class Response {
        private UUID       id;
        private UUID       payrollRunId;
        private UUID       employeeId;
        private int        month;
        private int        year;
        private int        daysInMonth;
        private int        daysPayable;
        private int        daysWorked;
        private BigDecimal lopDays;

        // Totals
        private BigDecimal grossEarnings;
        private BigDecimal totalDeductions;
        private BigDecimal netPay;

        // Statutory breakdowns
        private BigDecimal employeePf;
        private BigDecimal employeeEsi;
        private BigDecimal employeePt;
        private BigDecimal tds;
        private BigDecimal employerPf;
        private BigDecimal employerEsi;
        private BigDecimal employerLwf;

        // Full component breakdown
        private List<PayslipLineItem> components;

        private Payslip.PayslipStatus status;
        private String    pdfUrl;
        private Instant   emailedAt;
        private Instant   createdAt;
    }

    /** Lightweight summary for employee's payslip list. */
    @Getter @Setter @Builder
    public static class Summary {
        private UUID       id;
        private int        month;
        private int        year;
        private BigDecimal grossEarnings;
        private BigDecimal netPay;
        private BigDecimal tds;
        private Payslip.PayslipStatus status;
        private String     pdfUrl;
    }
}
