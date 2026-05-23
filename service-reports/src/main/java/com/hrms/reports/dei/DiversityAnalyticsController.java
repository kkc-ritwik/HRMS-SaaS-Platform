package com.hrms.reports.dei;

import com.hrms.security.model.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Diversity, Equity & Inclusion analytics. Queries are designed to be safe by default:
 *   · k-anonymity floor (suppresses groups with fewer than {@code MIN_GROUP_SIZE} members)
 *   · only aggregated counts and ratios are returned — no individual rows
 *   · employee_directory_view materialised view supplies gender / ethnicity / generation
 *
 * Endpoints:
 *   GET /api/reports/dei/headcount?by=gender|ethnicity|generation|disability
 *   GET /api/reports/dei/leadership-representation
 *   GET /api/reports/dei/hiring-funnel?year=
 *   GET /api/reports/dei/attrition-by-group?year=&by=
 *   GET /api/reports/dei/pay-gap?by=gender
 */
@RestController
@RequestMapping("/api/reports/dei")
@RequiredArgsConstructor
public class DiversityAnalyticsController {

    private static final int MIN_GROUP_SIZE = 5;

    @PersistenceContext private EntityManager em;

    @GetMapping("/headcount")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> headcount(@RequestParam(defaultValue = "gender") String by) {
        String column = safeColumn(by);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                "SELECT " + column + ", COUNT(*) FROM employee_directory_view " +
                "WHERE tenant_id = :t AND employment_status = 'ACTIVE' " +
                "GROUP BY " + column + " ORDER BY COUNT(*) DESC")
                .setParameter("t", TenantContext.get())
                .getResultList();
        return suppressSmallAndPercent(rows);
    }

    @GetMapping("/leadership-representation")
    @Transactional(readOnly = true)
    public Map<String, Object> leadership() {
        String t = TenantContext.get();
        @SuppressWarnings("unchecked")
        List<Object[]> all = em.createNativeQuery(
                "SELECT gender, COUNT(*) FROM employee_directory_view " +
                "WHERE tenant_id = :t AND employment_status = 'ACTIVE' GROUP BY gender")
                .setParameter("t", t).getResultList();
        @SuppressWarnings("unchecked")
        List<Object[]> leaders = em.createNativeQuery(
                "SELECT gender, COUNT(*) FROM employee_directory_view " +
                "WHERE tenant_id = :t AND employment_status = 'ACTIVE' AND grade_level >= 7 GROUP BY gender")
                .setParameter("t", t).getResultList();
        return Map.of(
                "overall", suppressSmallAndPercent(all),
                "leadership", suppressSmallAndPercent(leaders)
        );
    }

    @GetMapping("/hiring-funnel")
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> hiringFunnel(@RequestParam int year) {
        String t = TenantContext.get();
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);

        List<Object[]> rows = em.createNativeQuery(
                "SELECT c.gender, " +
                "  COUNT(DISTINCT a.candidate_id) AS applied, " +
                "  COUNT(DISTINCT CASE WHEN a.stage IN ('PHONE_SCREEN','TECHNICAL','HR','OFFER','HIRED') THEN a.candidate_id END) AS interviewed, " +
                "  COUNT(DISTINCT CASE WHEN a.stage IN ('OFFER','HIRED') THEN a.candidate_id END) AS offered, " +
                "  COUNT(DISTINCT CASE WHEN a.stage = 'HIRED' THEN a.candidate_id END) AS hired " +
                "FROM applications a JOIN candidates c ON c.id = a.candidate_id AND c.tenant_id = a.tenant_id " +
                "WHERE a.tenant_id = :t AND a.applied_at::date BETWEEN :from AND :to " +
                "GROUP BY c.gender")
                .setParameter("t", t)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] r : rows) {
            long applied = ((Number) r[1]).longValue();
            if (applied < MIN_GROUP_SIZE) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("group", r[0]);
            m.put("applied", applied);
            m.put("interviewed", ((Number) r[2]).longValue());
            m.put("offered", ((Number) r[3]).longValue());
            m.put("hired", ((Number) r[4]).longValue());
            out.add(m);
        }
        return out;
    }

    @GetMapping("/attrition-by-group")
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> attrition(@RequestParam int year, @RequestParam(defaultValue = "gender") String by) {
        String column = safeColumn(by);
        List<Object[]> rows = em.createNativeQuery(
                "SELECT " + column + ", COUNT(*) FILTER (WHERE EXTRACT(YEAR FROM exit_date) = :y) AS exits, " +
                "       COUNT(*) AS headcount " +
                "FROM employee_directory_view WHERE tenant_id = :t GROUP BY " + column)
                .setParameter("t", TenantContext.get())
                .setParameter("y", year)
                .getResultList();

        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] r : rows) {
            long headcount = ((Number) r[2]).longValue();
            if (headcount < MIN_GROUP_SIZE) continue;
            long exits = ((Number) r[1]).longValue();
            BigDecimal rate = BigDecimal.valueOf(exits * 100.0 / headcount).setScale(2, RoundingMode.HALF_UP);
            out.add(Map.of("group", r[0] == null ? "(unspecified)" : r[0],
                    "headcount", headcount, "exits", exits, "attritionPct", rate));
        }
        return out;
    }

    @GetMapping("/pay-gap")
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Map<String, Object> payGap(@RequestParam(defaultValue = "gender") String by) {
        String column = safeColumn(by);
        List<Object[]> rows = em.createNativeQuery(
                "SELECT " + column + ", AVG(annual_ctc), COUNT(*) FROM employee_directory_view " +
                "WHERE tenant_id = :t AND employment_status = 'ACTIVE' AND annual_ctc IS NOT NULL " +
                "GROUP BY " + column)
                .setParameter("t", TenantContext.get())
                .getResultList();

        Map<String, BigDecimal> means = new LinkedHashMap<>();
        long maxN = 0;
        String maxGroup = null;
        BigDecimal maxMean = BigDecimal.ZERO;
        for (Object[] r : rows) {
            long n = ((Number) r[2]).longValue();
            if (n < MIN_GROUP_SIZE) continue;
            BigDecimal mean = (BigDecimal) r[1];
            String g = r[0] == null ? "(unspecified)" : r[0].toString();
            means.put(g, mean);
            if (mean.compareTo(maxMean) > 0) { maxMean = mean; maxGroup = g; }
            maxN += n;
        }

        Map<String, BigDecimal> gap = new LinkedHashMap<>();
        if (maxGroup != null) {
            for (Map.Entry<String, BigDecimal> e : means.entrySet()) {
                BigDecimal pct = e.getValue()
                        .subtract(maxMean)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(maxMean, 2, RoundingMode.HALF_UP);
                gap.put(e.getKey(), pct);
            }
        }
        return Map.of("means", means, "baselineGroup", maxGroup, "gapPctVsBaseline", gap, "sampleSize", maxN);
    }

    private static String safeColumn(String by) {
        return switch (by) {
            case "gender" -> "gender";
            case "ethnicity" -> "ethnicity";
            case "generation" -> "generation";
            case "disability" -> "disability_status";
            case "nationality" -> "nationality";
            default -> "gender";
        };
    }

    private static List<Map<String, Object>> suppressSmallAndPercent(List<Object[]> rows) {
        long grandTotal = 0;
        for (Object[] r : rows) grandTotal += ((Number) r[1]).longValue();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] r : rows) {
            long n = ((Number) r[1]).longValue();
            if (n < MIN_GROUP_SIZE) continue;
            BigDecimal pct = grandTotal == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(n * 100.0 / grandTotal).setScale(2, RoundingMode.HALF_UP);
            out.add(Map.of("group", r[0] == null ? "(unspecified)" : r[0],
                    "count", n, "percent", pct));
        }
        return out;
    }
}
