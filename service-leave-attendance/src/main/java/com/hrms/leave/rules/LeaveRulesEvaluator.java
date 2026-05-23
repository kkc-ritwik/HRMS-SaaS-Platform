package com.hrms.leave.rules;

import com.hrms.leave.entity.LeavePolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Evaluates a leave request against the policy and returns the effective number of
 * leave days to deduct, plus any policy violations. Used by LeaveService BEFORE saving.
 *
 * Rules implemented (Zoho People parity):
 *
 *   · Sandwich rule        — if leave brackets a weekend/holiday on both sides, the
 *                            weekend/holiday counts as leave too. Common in PSU/Govt.
 *
 *   · Half-day             — 0.5-day deduction when allowHalfDay && request.halfDay=true.
 *
 *   · Short leave          — 0.25-day deduction when allowShortLeave (for ≤ 2-hour
 *                            errands during the day).
 *
 *   · Notice days          — request rejected if days-until-leave < policy.minNoticeDays
 *                            (skipped for emergency / bereavement types).
 *
 *   · Negative balance     — allowed only if policy permits AND new balance ≥ -maxNegativeDays.
 *
 *   · Pro-rata             — accrual capped to fraction of year completed when joiner/leaver.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveRulesEvaluator {

    public Evaluation evaluate(LeaveRequest req, LeavePolicy policy, Set<LocalDate> holidays) {
        Evaluation out = new Evaluation();
        out.violations = new ArrayList<>();
        out.warnings = new ArrayList<>();

        // 1. Notice period check
        if (policy.getMinNoticeDays() > 0 && !req.emergency) {
            long noticeGiven = ChronoUnit.DAYS.between(req.requestedOn, req.fromDate);
            if (noticeGiven < policy.getMinNoticeDays()) {
                out.violations.add("Minimum notice of " + policy.getMinNoticeDays()
                        + " days required (only " + noticeGiven + " given)");
            }
        }

        // 2. Short leave (≤ 2-hour) — quarter-day
        if (req.shortLeave) {
            if (!policy.isAllowShortLeave()) {
                out.violations.add("Short leave not allowed by policy");
            }
            out.daysDeducted = new BigDecimal("0.25");
            out.applicableDays = req.fromDate == null ? List.of() : List.of(req.fromDate);
            return out;
        }

        // 3. Half-day
        if (req.halfDay) {
            if (!policy.isAllowHalfDay()) out.violations.add("Half-day not allowed by policy");
            out.daysDeducted = new BigDecimal("0.5");
            out.applicableDays = List.of(req.fromDate);
            return out;
        }

        // 4. Apply sandwich rule (if enabled, intervening weekends/holidays count too)
        List<LocalDate> chargeable = new ArrayList<>();
        LocalDate cursor = req.fromDate;
        while (!cursor.isAfter(req.toDate)) {
            boolean isOffDay = cursor.getDayOfWeek() == DayOfWeek.SATURDAY
                    || cursor.getDayOfWeek() == DayOfWeek.SUNDAY
                    || holidays.contains(cursor);
            if (!isOffDay) {
                chargeable.add(cursor);
            } else if (policy.isSandwichRuleEnabled() && bracketedByLeave(cursor, req.fromDate, req.toDate, holidays)) {
                chargeable.add(cursor);
                out.warnings.add("Sandwich rule: " + cursor + " (off-day) counted as leave");
            }
            cursor = cursor.plusDays(1);
        }

        out.daysDeducted = BigDecimal.valueOf(chargeable.size()).setScale(2, RoundingMode.HALF_UP);
        out.applicableDays = chargeable;

        // 5. Negative balance
        BigDecimal projectedBalance = req.currentBalance.subtract(out.daysDeducted);
        if (projectedBalance.signum() < 0) {
            if (!policy.isNegativeBalanceAllowed()) {
                out.violations.add("Insufficient balance (would go to " + projectedBalance + ")");
            } else if (policy.getMaxNegativeDays() != null
                    && projectedBalance.abs().compareTo(policy.getMaxNegativeDays()) > 0) {
                out.violations.add("Exceeds permitted negative balance of "
                        + policy.getMaxNegativeDays() + " days");
            } else {
                out.warnings.add("Will result in negative balance: " + projectedBalance);
            }
        }
        return out;
    }

    /**
     * Year-end carry-forward processor. Returns the amount that carries into next year.
     * Implements: min(currentBalance, policy.maxCarryForward); the rest auto-encashes
     * if encashmentEnabled, else lapses.
     */
    public CarryForwardResult processYearEnd(BigDecimal currentBalance, LeavePolicy policy) {
        CarryForwardResult r = new CarryForwardResult();
        r.startingBalance = currentBalance;
        if (!policy.isCarryForwardEnabled()) {
            r.carriedForward = BigDecimal.ZERO;
            r.encashed = policy.isEncashmentEnabled() ? cap(currentBalance, policy.getMaxEncashmentDays()) : BigDecimal.ZERO;
            r.lapsed = currentBalance.subtract(r.encashed).max(BigDecimal.ZERO);
            return r;
        }
        BigDecimal cap = policy.getMaxCarryForward() == null ? currentBalance : policy.getMaxCarryForward();
        r.carriedForward = currentBalance.min(cap).max(BigDecimal.ZERO);
        BigDecimal excess = currentBalance.subtract(r.carriedForward).max(BigDecimal.ZERO);
        if (policy.isEncashmentEnabled()) {
            r.encashed = cap(excess, policy.getMaxEncashmentDays());
            r.lapsed = excess.subtract(r.encashed);
        } else {
            r.encashed = BigDecimal.ZERO;
            r.lapsed = excess;
        }
        return r;
    }

    /** Pro-rated accrual for a partial year (joiner / leaver). */
    public BigDecimal proRataAccrual(LeavePolicy policy, LocalDate hireOrExitDate, LocalDate yearStart, LocalDate yearEnd) {
        long workingDays = ChronoUnit.DAYS.between(yearStart, yearEnd) + 1;
        long actualDays = ChronoUnit.DAYS.between(hireOrExitDate, yearEnd) + 1;
        if (actualDays <= 0) return BigDecimal.ZERO;
        return policy.getAccrualAmount()
                .multiply(BigDecimal.valueOf(actualDays))
                .divide(BigDecimal.valueOf(workingDays), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal cap(BigDecimal value, BigDecimal max) {
        if (max == null) return value;
        return value.min(max);
    }

    private static boolean bracketedByLeave(LocalDate day, LocalDate from, LocalDate to, Set<LocalDate> holidays) {
        // Sandwich applies only if both the day before AND day after are within the request span (or also off-days)
        LocalDate before = day.minusDays(1);
        LocalDate after = day.plusDays(1);
        boolean prevIsLeave = !before.isBefore(from) && !before.isAfter(to);
        boolean nextIsLeave = !after.isBefore(from) && !after.isAfter(to);
        return prevIsLeave && nextIsLeave;
    }

    public static class LeaveRequest {
        public LocalDate fromDate;
        public LocalDate toDate;
        public LocalDate requestedOn;
        public boolean halfDay;
        public boolean shortLeave;
        public boolean emergency;
        public BigDecimal currentBalance;
    }

    public static class Evaluation {
        public BigDecimal daysDeducted;
        public List<LocalDate> applicableDays;
        public List<String> violations;
        public List<String> warnings;
        public boolean isAllowed() { return violations == null || violations.isEmpty(); }
    }

    public static class CarryForwardResult {
        public BigDecimal startingBalance;
        public BigDecimal carriedForward;
        public BigDecimal encashed;
        public BigDecimal lapsed;
    }
}
