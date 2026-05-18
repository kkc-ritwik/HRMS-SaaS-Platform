package com.hrms.payroll.service.statutory;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Indian Payment of Gratuity Act, 1972 calculator.
 *   Gratuity = (last drawn basic + DA) * 15 / 26 * years-of-service.
 *   Eligible only after >= 5 years (4y 240d in qualifying years rule waived for simplicity).
 *   Capped at INR 20,00,000 (statutory ceiling).
 */
@Service
public class GratuityCalculator {

    private static final BigDecimal DAYS = new BigDecimal("26");
    private static final BigDecimal HALF_MONTH_DAYS = new BigDecimal("15");
    private static final BigDecimal CAP = new BigDecimal("2000000");

    public Result calculate(BigDecimal lastBasicPlusDa, LocalDate joinDate, LocalDate exitDate) {
        if (lastBasicPlusDa == null || joinDate == null || exitDate == null
                || exitDate.isBefore(joinDate)) return new Result(BigDecimal.ZERO, false, "Invalid input", 0);

        long days = ChronoUnit.DAYS.between(joinDate, exitDate);
        double years = days / 365.25;
        boolean eligible = years >= 5.0;
        int wholeYears = (int) Math.floor(years);
        // If extra > 6 months, round up by 1
        double remainder = years - wholeYears;
        if (remainder > 0.5) wholeYears += 1;

        BigDecimal amount = lastBasicPlusDa
                .multiply(HALF_MONTH_DAYS).divide(DAYS, 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(wholeYears))
                .setScale(2, RoundingMode.HALF_UP);

        if (amount.compareTo(CAP) > 0) amount = CAP;
        return new Result(eligible ? amount : BigDecimal.ZERO, eligible,
                eligible ? "OK" : "Less than 5 years of service", wholeYears);
    }

    public record Result(BigDecimal amount, boolean eligible, String reason, int countedYears) {}
}
