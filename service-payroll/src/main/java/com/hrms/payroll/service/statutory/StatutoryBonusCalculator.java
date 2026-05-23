package com.hrms.payroll.service.statutory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Statutory Bonus calculation per the Payment of Bonus Act, 1965 (India).
 *
 * Rules:
 *  - Eligibility: employees with basic + DA ≤ ₹21,000/month and ≥ 30 working days in the year.
 *  - Wage ceiling for calculation: ₹7,000/month or minimum wage of the state (whichever is higher).
 *  - Minimum bonus: 8.33% of annual wages.
 *  - Maximum bonus: 20% of annual wages (only if allocable surplus permits).
 *
 * The actual percentage between min and max is driven by company's allocable surplus —
 * passed in as `declaredPercent`. If the percent is between 8.33 and 20, it's used as-is;
 * outside that range it's clamped.
 */
@Slf4j
@Service
public class StatutoryBonusCalculator {

    /** Wage ceiling for calculation (₹7,000/month). */
    public static final BigDecimal WAGE_CEILING_PER_MONTH = new BigDecimal("7000");
    /** Eligibility ceiling on basic+DA. */
    public static final BigDecimal ELIGIBILITY_CEILING_PER_MONTH = new BigDecimal("21000");
    public static final BigDecimal MIN_BONUS_PCT = new BigDecimal("8.33");
    public static final BigDecimal MAX_BONUS_PCT = new BigDecimal("20.00");

    public Result calculate(BigDecimal basicPlusDaPerMonth, int workingDaysInYear,
                             BigDecimal stateMinimumWagePerMonth, BigDecimal declaredPercent) {
        if (basicPlusDaPerMonth == null || basicPlusDaPerMonth.signum() <= 0) {
            return new Result(BigDecimal.ZERO, false, "No basic+DA", null);
        }
        if (workingDaysInYear < 30) {
            return new Result(BigDecimal.ZERO, false, "< 30 working days", null);
        }
        if (basicPlusDaPerMonth.compareTo(ELIGIBILITY_CEILING_PER_MONTH) > 0) {
            return new Result(BigDecimal.ZERO, false,
                    "Above ₹21,000/month eligibility ceiling", null);
        }

        // Calculation wage = lower of (actual basic+DA, max(₹7000, state minimum wage))
        BigDecimal calcCeiling = WAGE_CEILING_PER_MONTH;
        if (stateMinimumWagePerMonth != null && stateMinimumWagePerMonth.compareTo(calcCeiling) > 0) {
            calcCeiling = stateMinimumWagePerMonth;
        }
        BigDecimal calcWage = basicPlusDaPerMonth.compareTo(calcCeiling) > 0 ? calcCeiling : basicPlusDaPerMonth;

        BigDecimal annualWage = calcWage.multiply(BigDecimal.valueOf(12));

        BigDecimal pct = declaredPercent == null ? MIN_BONUS_PCT : declaredPercent;
        if (pct.compareTo(MIN_BONUS_PCT) < 0) pct = MIN_BONUS_PCT;
        if (pct.compareTo(MAX_BONUS_PCT) > 0) pct = MAX_BONUS_PCT;

        BigDecimal bonus = annualWage.multiply(pct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        log.debug("Statutory bonus: basicDA={} calcWage={} annualWage={} pct={} bonus={}",
                basicPlusDaPerMonth, calcWage, annualWage, pct, bonus);
        return new Result(bonus, true, "OK", pct);
    }

    public record Result(BigDecimal amount, boolean eligible, String reason, BigDecimal appliedPercent) {}
}
