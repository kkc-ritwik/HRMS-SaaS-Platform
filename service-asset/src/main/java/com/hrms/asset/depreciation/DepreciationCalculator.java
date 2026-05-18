package com.hrms.asset.depreciation;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Asset depreciation calculator. Supports the two methods that cover ~95% of HR-managed assets:
 *   - STRAIGHT_LINE: (cost - salvage) / useful-life-years per year
 *   - WRITTEN_DOWN_VALUE (declining-balance): book * rate per year
 *
 * Output is current book value as of a given date. Persistence of monthly depreciation
 * journal entries is left to the caller (AssetDepreciationService) which records each
 * period's expense as an AssetDepreciationEntry.
 */
@Service
public class DepreciationCalculator {

    public BigDecimal bookValueAsOf(BigDecimal cost, BigDecimal salvage, BigDecimal annualRate,
                                     LocalDate purchaseDate, LocalDate asOf, Method method, int usefulLifeYears) {
        if (cost == null || purchaseDate == null) return BigDecimal.ZERO;
        if (asOf == null) asOf = LocalDate.now();
        if (asOf.isBefore(purchaseDate)) return cost;
        BigDecimal sal = salvage == null ? BigDecimal.ZERO : salvage;

        long monthsElapsed = ChronoUnit.MONTHS.between(purchaseDate, asOf);
        double years = monthsElapsed / 12.0;

        return switch (method) {
            case STRAIGHT_LINE -> {
                if (usefulLifeYears <= 0) yield cost;
                BigDecimal annual = cost.subtract(sal)
                        .divide(BigDecimal.valueOf(usefulLifeYears), 6, RoundingMode.HALF_UP);
                BigDecimal acc = annual.multiply(BigDecimal.valueOf(years));
                BigDecimal book = cost.subtract(acc);
                yield book.compareTo(sal) < 0 ? sal : book.setScale(2, RoundingMode.HALF_UP);
            }
            case WRITTEN_DOWN_VALUE -> {
                if (annualRate == null) yield cost;
                double rate = annualRate.doubleValue() / 100.0;
                double remainingFactor = Math.pow(1.0 - rate, years);
                BigDecimal book = cost.multiply(BigDecimal.valueOf(remainingFactor));
                yield book.compareTo(sal) < 0 ? sal : book.setScale(2, RoundingMode.HALF_UP);
            }
        };
    }

    /** Monthly depreciation expense (constant under straight-line; varying under WDV). */
    public BigDecimal monthlyDepreciation(BigDecimal cost, BigDecimal salvage, BigDecimal annualRate,
                                           int usefulLifeYears, Method method) {
        if (cost == null) return BigDecimal.ZERO;
        BigDecimal sal = salvage == null ? BigDecimal.ZERO : salvage;
        return switch (method) {
            case STRAIGHT_LINE -> usefulLifeYears <= 0 ? BigDecimal.ZERO
                    : cost.subtract(sal)
                        .divide(BigDecimal.valueOf(usefulLifeYears * 12L), 2, RoundingMode.HALF_UP);
            case WRITTEN_DOWN_VALUE -> annualRate == null ? BigDecimal.ZERO
                    : cost.multiply(annualRate).divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP);
        };
    }

    public enum Method { STRAIGHT_LINE, WRITTEN_DOWN_VALUE }
}
