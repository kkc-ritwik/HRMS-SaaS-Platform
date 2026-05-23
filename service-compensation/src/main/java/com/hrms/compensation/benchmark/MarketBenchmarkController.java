package com.hrms.compensation.benchmark;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/compensation/benchmark")
@RequiredArgsConstructor
public class MarketBenchmarkController {

    public interface Repo extends JpaRepository<MarketBenchmark, UUID> {
        @Query("SELECT b FROM MarketBenchmark b WHERE b.tenantId = :t AND b.roleCode = :role " +
                "AND b.country = :country AND (:level IS NULL OR b.level = :level) " +
                "ORDER BY b.surveyDate DESC")
        List<MarketBenchmark> find(@Param("t") String tenant,
                                   @Param("role") String role,
                                   @Param("country") String country,
                                   @Param("level") String level);
    }

    private final Repo repo;
    private final BenchmarkProviderClient provider;

    @GetMapping
    public List<MarketBenchmark> lookup(@RequestParam String roleCode,
                                        @RequestParam(defaultValue = "IN") String country,
                                        @RequestParam(required = false) String level) {
        return repo.find(TenantContext.get(), roleCode, country, level);
    }

    @PostMapping("/refresh")
    @Transactional
    public List<MarketBenchmark> refresh(@RequestBody Map<String, Object> body) {
        String tenant = TenantContext.get();
        List<MarketBenchmark> fresh = provider.fetch(tenant,
                (String) body.getOrDefault("provider", "MERCER"),
                (String) body.get("roleCode"),
                (String) body.getOrDefault("country", "IN"),
                (String) body.get("level"));
        return repo.saveAll(fresh);
    }

    @PostMapping("/bulk-upload")
    @Transactional
    public Map<String, Object> bulkUpload(@RequestBody List<MarketBenchmark> rows) {
        String tenant = TenantContext.get();
        for (MarketBenchmark b : rows) b.setTenantId(tenant);
        repo.saveAll(rows);
        return Map.of("imported", rows.size());
    }

    @GetMapping("/compa-ratio")
    public Map<String, Object> compaRatio(@RequestParam BigDecimal currentCtc,
                                          @RequestParam String roleCode,
                                          @RequestParam(defaultValue = "IN") String country) {
        List<MarketBenchmark> latest = repo.find(TenantContext.get(), roleCode, country, null);
        if (latest.isEmpty()) return Map.of("error", "No benchmark data");
        BigDecimal median = latest.get(0).getP50TotalCash();
        BigDecimal ratio = provider.compaRatio(currentCtc, median);
        String zone = ratio == null ? "UNKNOWN"
                : ratio.compareTo(BigDecimal.valueOf(80)) < 0 ? "BELOW_MARKET"
                : ratio.compareTo(BigDecimal.valueOf(120)) > 0 ? "ABOVE_MARKET" : "AT_MARKET";
        return Map.of("currentCtc", currentCtc, "marketMedian", median,
                "compaRatio", ratio, "zone", zone,
                "benchmarkAsOf", latest.get(0).getSurveyDate().toString());
    }
}
