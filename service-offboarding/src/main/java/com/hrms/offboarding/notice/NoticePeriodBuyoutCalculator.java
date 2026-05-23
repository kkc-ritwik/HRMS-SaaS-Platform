package com.hrms.offboarding.notice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * Calculate buyout / shortfall for an employee who serves less than the contractual
 * notice period. Rules per company policy vary; this implements the most common
 * Indian SaaS pattern:
 *
 *   shortfall_days = max(0, contractual_notice_days − actual_notice_days)
 *   recovery       = shortfall_days × (monthly_gross / 30)
 *
 * The employer recovers from F&F; the employee can also "buy out" voluntarily.
 * The reverse case (employee serves more days than required → no payback).
 *
 * Optionally supports:
 *   · pro-rating by working days only (excludes weekends/holidays)
 *   · capping recovery at N months of gross
 *   · waiver percentage (HR can waive part of the shortfall)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticePeriodBuyoutCalculator {

    public BuyoutResult compute(BuyoutRequest req) {
        long actualDays = ChronoUnit.DAYS.between(req.resignationDate, req.lastWorkingDay);
        if (actualDays < 0) actualDays = 0;

        long shortfallDays = Math.max(0, req.contractualNoticeDays - actualDays);
        long excessDays    = Math.max(0, actualDays - req.contractualNoticeDays);

        BigDecimal perDay = req.monthlyGross.divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP);
        BigDecimal grossRecovery = perDay.multiply(BigDecimal.valueOf(shortfallDays));

        BigDecimal waiverAmount = grossRecovery
                .multiply(req.waiverPercent == null ? BigDecimal.ZERO : req.waiverPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal netRecovery = grossRecovery.subtract(waiverAmount).max(BigDecimal.ZERO);

        if (req.capMonths != null) {
            BigDecimal cap = req.monthlyGross.multiply(BigDecimal.valueOf(req.capMonths));
            if (netRecovery.compareTo(cap) > 0) netRecovery = cap;
        }

        BuyoutResult out = new BuyoutResult();
        out.contractualNoticeDays = req.contractualNoticeDays;
        out.actualNoticeDays = actualDays;
        out.shortfallDays = shortfallDays;
        out.excessDays = excessDays;
        out.perDayGross = perDay.setScale(2, RoundingMode.HALF_UP);
        out.grossRecovery = grossRecovery.setScale(2, RoundingMode.HALF_UP);
        out.waiverAmount = waiverAmount;
        out.netRecovery = netRecovery.setScale(2, RoundingMode.HALF_UP);
        out.breakdown = Map.of(
                "shortfallDays", shortfallDays,
                "perDay", out.perDayGross,
                "gross", out.grossRecovery,
                "waiverPercent", req.waiverPercent == null ? BigDecimal.ZERO : req.waiverPercent,
                "waiverAmount", waiverAmount,
                "capMonths", req.capMonths == null ? "" : req.capMonths,
                "net", out.netRecovery
        );
        return out;
    }

    public static class BuyoutRequest {
        public LocalDate resignationDate;
        public LocalDate lastWorkingDay;
        public int contractualNoticeDays;          // e.g. 90
        public BigDecimal monthlyGross;             // employee's monthly gross
        public BigDecimal waiverPercent;            // 0-100 (null = none)
        public Integer capMonths;                   // recovery capped at N months gross (null = uncapped)
    }

    public static class BuyoutResult {
        public long contractualNoticeDays;
        public long actualNoticeDays;
        public long shortfallDays;
        public long excessDays;
        public BigDecimal perDayGross;
        public BigDecimal grossRecovery;
        public BigDecimal waiverAmount;
        public BigDecimal netRecovery;
        public Map<String, Object> breakdown;
    }
}
