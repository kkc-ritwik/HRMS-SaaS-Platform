package com.hrms.leave.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leave_policies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LeavePolicy extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "leave_type_id", nullable = false)
    private UUID leaveTypeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "accrual_type", nullable = false, length = 20)
    private AccrualType accrualType = AccrualType.YEARLY;

    @Column(name = "accrual_amount", nullable = false, precision = 6, scale = 2)
    private BigDecimal accrualAmount = BigDecimal.ZERO;

    @Column(name = "max_accrual", precision = 6, scale = 2)
    private BigDecimal maxAccrual;

    @Column(name = "carry_forward_enabled", nullable = false)
    private boolean carryForwardEnabled = false;

    @Column(name = "max_carry_forward", precision = 6, scale = 2)
    private BigDecimal maxCarryForward;

    @Column(name = "carry_forward_expiry_months")
    private Integer carryForwardExpiryMonths;

    @Column(name = "encashment_enabled", nullable = false)
    private boolean encashmentEnabled = false;

    @Column(name = "max_encashment_days", precision = 6, scale = 2)
    private BigDecimal maxEncashmentDays;

    @Column(name = "pro_rata_on_joining", nullable = false)
    private boolean proRataOnJoining = false;

    @Column(name = "pro_rata_on_exit", nullable = false)
    private boolean proRataOnExit = false;

    @Column(name = "negative_balance_allowed", nullable = false)
    private boolean negativeBalanceAllowed = false;

    @Column(name = "max_negative_days", precision = 6, scale = 2)
    private BigDecimal maxNegativeDays;

    @Column(name = "sandwich_rule_enabled", nullable = false)
    private boolean sandwichRuleEnabled = false;

    @Column(name = "min_notice_days", nullable = false)
    private int minNoticeDays = 0;

    @Column(name = "allow_half_day", nullable = false)
    private boolean allowHalfDay = true;

    @Column(name = "allow_short_leave", nullable = false)
    private boolean allowShortLeave = false;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public enum AccrualType {
        MONTHLY, QUARTERLY, YEARLY, ON_HIRE_DATE
    }
}
