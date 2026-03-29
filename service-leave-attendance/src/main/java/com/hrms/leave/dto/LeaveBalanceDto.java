package com.hrms.leave.dto;

import com.hrms.leave.entity.LeaveAdjustment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class LeaveBalanceDto {

    @Getter @Setter @Builder
    public static class Response {
        private UUID id;
        private UUID employeeId;
        private UUID leaveTypeId;
        private String leaveTypeName;
        private String leaveTypeColor;
        private boolean leaveTypePaid;
        private int year;
        private BigDecimal openingBalance;
        private BigDecimal accrued;
        private BigDecimal used;
        private BigDecimal carryForward;
        private BigDecimal encashed;
        private BigDecimal adjusted;
        private BigDecimal available;
    }

    @Getter @Setter
    public static class SummaryResponse {
        private UUID employeeId;
        private int year;
        private List<Response> balances;
    }

    @Getter @Setter
    public static class AdjustRequest {
        @NotNull private UUID employeeId;
        @NotNull private UUID leaveTypeId;
        @NotNull private LeaveAdjustment.AdjustmentType adjustmentType;
        @NotNull @Positive private BigDecimal days;
        private String reason;
        @NotNull private LocalDate effectiveDate;
    }

    @Getter @Setter
    public static class InitRequest {
        @NotNull private Integer year;
        private UUID employeeId;  // null = init for all employees in tenant
    }
}
