package com.hrms.payroll.dto;

import com.hrms.payroll.entity.Loan;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class LoanDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank private String     loanType;
        @NotNull  private BigDecimal principalAmount;

        /** Annual interest rate (%). Set 0 for interest-free advances. */
        private BigDecimal interestRate = BigDecimal.ZERO;

        @NotNull @Min(1) private Integer tenureMonths;

        @NotNull private LocalDate disbursementDate;

        /** First payroll month from which EMI deduction starts. Defaults to next month. */
        private LocalDate startDeductionMonth;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID       id;
        private UUID       employeeId;
        private String     loanType;
        private BigDecimal principalAmount;
        private BigDecimal interestRate;
        private int        tenureMonths;
        private BigDecimal emiAmount;
        private LocalDate  disbursementDate;
        private LocalDate  startDeductionMonth;
        private BigDecimal outstandingBalance;
        private Loan.LoanStatus status;
        private int        repaymentsMade;
        private Instant    createdAt;
        private Instant    updatedAt;
    }

    /** One row in the amortization schedule. */
    @Getter @Setter @Builder
    public static class RepaymentScheduleItem {
        private int        emiNumber;
        private BigDecimal openingBalance;
        private BigDecimal emiAmount;
        private BigDecimal principalPart;
        private BigDecimal interestPart;
        private BigDecimal closingBalance;
        private boolean    paid;
    }
}
