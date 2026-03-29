package com.hrms.leave.dto;

import com.hrms.leave.entity.LeavePolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class LeavePolicyDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank private String name;
        @NotNull  private UUID leaveTypeId;
        @NotNull  private LeavePolicy.AccrualType accrualType;
        @NotNull  private BigDecimal accrualAmount;
        private BigDecimal maxAccrual;
        private boolean carryForwardEnabled;
        private BigDecimal maxCarryForward;
        private Integer carryForwardExpiryMonths;
        private boolean encashmentEnabled;
        private BigDecimal maxEncashmentDays;
        private boolean proRataOnJoining;
        private boolean proRataOnExit;
        private boolean negativeBalanceAllowed;
        private BigDecimal maxNegativeDays;
        private boolean sandwichRuleEnabled;
        private int minNoticeDays;
        private boolean allowHalfDay = true;
        private boolean allowShortLeave;
        @NotNull private LocalDate effectiveFrom;
        private LocalDate effectiveTo;
    }

    @Getter @Setter
    public static class UpdateRequest {
        private String name;
        private LeavePolicy.AccrualType accrualType;
        private BigDecimal accrualAmount;
        private BigDecimal maxAccrual;
        private Boolean carryForwardEnabled;
        private BigDecimal maxCarryForward;
        private Integer carryForwardExpiryMonths;
        private Boolean encashmentEnabled;
        private BigDecimal maxEncashmentDays;
        private Boolean negativeBalanceAllowed;
        private BigDecimal maxNegativeDays;
        private Boolean sandwichRuleEnabled;
        private Integer minNoticeDays;
        private Boolean allowHalfDay;
        private Boolean allowShortLeave;
        private LocalDate effectiveTo;
        private Boolean active;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID id;
        private String name;
        private UUID leaveTypeId;
        private String leaveTypeName;
        private LeavePolicy.AccrualType accrualType;
        private BigDecimal accrualAmount;
        private BigDecimal maxAccrual;
        private boolean carryForwardEnabled;
        private BigDecimal maxCarryForward;
        private Integer carryForwardExpiryMonths;
        private boolean encashmentEnabled;
        private BigDecimal maxEncashmentDays;
        private boolean proRataOnJoining;
        private boolean proRataOnExit;
        private boolean negativeBalanceAllowed;
        private BigDecimal maxNegativeDays;
        private boolean sandwichRuleEnabled;
        private int minNoticeDays;
        private boolean allowHalfDay;
        private boolean allowShortLeave;
        private LocalDate effectiveFrom;
        private LocalDate effectiveTo;
        private boolean active;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
