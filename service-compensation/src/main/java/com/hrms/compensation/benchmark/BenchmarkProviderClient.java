package com.hrms.compensation.benchmark;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Stub-mode connector to compensation benchmarking providers. Today this returns
 * canned percentile bands so the rest of the platform (PayGrade calibration, salary
 * revision proposals, recruiting offer recommendations) can consume them without
 * waiting on real survey access.
 *
 * Go-live wiring:
 *   · Mercer TRS  — REST endpoint mercer.com/services/trs/v1/query (subscription key)
 *   · AON Radford — radford.aon.com/RadfordAPI/query (OAuth client credentials)
 *   · WTW         — sales-research.wtwco.com/api/v1/cuts (api-key header)
 *
 * Set hrms.benchmark.mode=LIVE and provide hrms.benchmark.{provider}.api-key to switch.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BenchmarkProviderClient {

    @Value("${hrms.benchmark.mode:STUB}")
    private String mode;

    public List<MarketBenchmark> fetch(String tenantId, String provider,
                                       String roleCode, String country, String level) {
        if (!"STUB".equalsIgnoreCase(mode)) {
            throw new UnsupportedOperationException("Live benchmark integration not wired: " + provider);
        }
        log.info("[STUB-MODE] Returning canned benchmark data for {} / {} / {} / {}",
                provider, roleCode, country, level);

        MarketBenchmark b = new MarketBenchmark();
        b.setTenantId(tenantId);
        b.setProvider(provider == null ? "MERCER" : provider);
        b.setRoleCode(roleCode);
        b.setLevel(level);
        b.setCountry(country == null ? "IN" : country);
        b.setCurrency("IN".equals(country) ? "INR" : "USD");
        b.setSurveyDate(LocalDate.now().withDayOfMonth(1));

        BigDecimal base = "IN".equals(country) ? new BigDecimal("1500000") : new BigDecimal("100000");
        b.setP10TotalCash(base.multiply(new BigDecimal("0.7")));
        b.setP25TotalCash(base.multiply(new BigDecimal("0.85")));
        b.setP50TotalCash(base);
        b.setP75TotalCash(base.multiply(new BigDecimal("1.20")));
        b.setP90TotalCash(base.multiply(new BigDecimal("1.45")));
        b.setP50Base(base.multiply(new BigDecimal("0.85")));
        b.setP50Variable(base.multiply(new BigDecimal("0.10")));
        b.setP50Equity(base.multiply(new BigDecimal("0.05")));
        b.setSampleSize(100);

        return List.of(b);
    }

    /** Compute compa-ratio = currentCtc / market median × 100. < 80 = underpaid, >120 = above market. */
    public BigDecimal compaRatio(BigDecimal currentCtc, BigDecimal marketMedian) {
        if (marketMedian == null || marketMedian.signum() == 0) return null;
        return currentCtc.multiply(BigDecimal.valueOf(100))
                .divide(marketMedian, 2, java.math.RoundingMode.HALF_UP);
    }
}
