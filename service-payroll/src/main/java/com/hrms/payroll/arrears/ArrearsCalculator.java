package com.hrms.payroll.arrears;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Computes salary arrears when a back-dated revision is applied.
 * Example: revision is approved on 15-Jun for an effective date of 01-Apr.
 * The 3 months Apr/May/Jun need a top-up = (new_gross - old_gross) per month.
 *
 * Bonus shares of the arrears (e.g. revised HRA) are included by passing the gross deltas.
 * TDS implication is handled by the regular payroll engine in the next run.
 */
@Slf4j
@Service
public class ArrearsCalculator {

    /**
     * Compute one arrears line per month between effectiveFrom and revisionDate (inclusive of start month).
     */
    public List<ArrearsLine> compute(BigDecimal oldGrossMonthly, BigDecimal newGrossMonthly,
                                      LocalDate effectiveFrom, LocalDate revisionApprovedOn) {
        if (oldGrossMonthly == null || newGrossMonthly == null) return List.of();
        BigDecimal delta = newGrossMonthly.subtract(oldGrossMonthly);
        if (delta.signum() == 0) return List.of();
        if (effectiveFrom == null || revisionApprovedOn == null
                || revisionApprovedOn.isBefore(effectiveFrom)) return List.of();

        List<ArrearsLine> lines = new ArrayList<>();
        YearMonth start = YearMonth.from(effectiveFrom);
        YearMonth end = YearMonth.from(revisionApprovedOn).minusMonths(1);
        // arrears settle separately for the current month — that month gets the new rate directly.
        if (end.isBefore(start)) return List.of();

        YearMonth cur = start;
        while (!cur.isAfter(end)) {
            BigDecimal pro = delta;
            if (cur.equals(start) && effectiveFrom.getDayOfMonth() > 1) {
                int totalDays = cur.lengthOfMonth();
                int eligibleDays = (int) ChronoUnit.DAYS.between(effectiveFrom, cur.atEndOfMonth()) + 1;
                pro = delta.multiply(BigDecimal.valueOf(eligibleDays))
                        .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP);
            }
            lines.add(new ArrearsLine(cur.getYear(), cur.getMonthValue(), pro));
            cur = cur.plusMonths(1);
        }
        BigDecimal total = lines.stream().map(ArrearsLine::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("Arrears computed: {} → {} months, total ₹{}", delta, lines.size(), total);
        return lines;
    }

    public record ArrearsLine(int year, int month, BigDecimal amount) {}
}
