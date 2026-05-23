package com.hrms.engagement.pulse;

import com.hrms.security.model.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Engagement heatmaps — slice eNPS / pulse scores by department, location, manager, tenure band.
 * Backs Zoho People's "engagement at a glance" tile and helps HR drill into hotspots.
 *
 * NOTE: department / location / manager / hire-date are denormalised onto the check-in via a
 * scheduled enricher (out of scope here). The queries assume those fields exist on the
 * employee_directory_view materialised view that core-hr populates.
 */
@RestController
@RequestMapping("/api/engagement/heatmap")
@RequiredArgsConstructor
public class EngagementHeatmapController {

    @PersistenceContext private EntityManager em;

    /** Heatmap by department. */
    @GetMapping("/by-department")
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> byDepartment(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        return bucket("e.department_id", "e.department_name", from, to);
    }

    /** Heatmap by office location. */
    @GetMapping("/by-location")
    public List<Map<String, Object>> byLocation(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        return bucket("e.location_id", "e.location_name", from, to);
    }

    /** Heatmap by direct-line manager. */
    @GetMapping("/by-manager")
    public List<Map<String, Object>> byManager(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        return bucket("e.manager_id", "e.manager_name", from, to);
    }

    /** Heatmap by tenure band — 0-1y / 1-3y / 3-5y / 5y+. */
    @GetMapping("/by-tenure")
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> byTenure(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        String tenant = TenantContext.get();
        List<Object[]> rows = em.createNativeQuery(
                "SELECT CASE " +
                "         WHEN EXTRACT(YEAR FROM AGE(NOW(), e.hire_date)) < 1 THEN '0-1y' " +
                "         WHEN EXTRACT(YEAR FROM AGE(NOW(), e.hire_date)) < 3 THEN '1-3y' " +
                "         WHEN EXTRACT(YEAR FROM AGE(NOW(), e.hire_date)) < 5 THEN '3-5y' " +
                "         ELSE '5y+' END AS band, " +
                "       COUNT(m.id), AVG(m.score), " +
                "       SUM(CASE WHEN m.score >= 4 THEN 1 ELSE 0 END), " +
                "       SUM(CASE WHEN m.score <= 2 THEN 1 ELSE 0 END) " +
                "FROM engagement_mood_checkins m " +
                "JOIN employee_directory_view e ON e.employee_id = m.employee_id AND e.tenant_id = m.tenant_id " +
                "WHERE m.tenant_id = :t AND m.check_in_date BETWEEN :from AND :to AND m.is_deleted = false " +
                "GROUP BY band ORDER BY band")
                .setParameter("t", tenant)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        return rowsToBuckets(rows);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> bucket(String idCol, String nameCol,
                                             LocalDate from, LocalDate to) {
        String tenant = TenantContext.get();
        List<Object[]> rows = em.createNativeQuery(
                "SELECT " + idCol + ", " + nameCol + ", " +
                "       COUNT(m.id), AVG(m.score), " +
                "       SUM(CASE WHEN m.score >= 4 THEN 1 ELSE 0 END), " +
                "       SUM(CASE WHEN m.score <= 2 THEN 1 ELSE 0 END) " +
                "FROM engagement_mood_checkins m " +
                "JOIN employee_directory_view e ON e.employee_id = m.employee_id AND e.tenant_id = m.tenant_id " +
                "WHERE m.tenant_id = :t AND m.check_in_date BETWEEN :from AND :to AND m.is_deleted = false " +
                "GROUP BY " + idCol + ", " + nameCol + " " +
                "ORDER BY AVG(m.score) ASC")
                .setParameter("t", tenant)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] r : rows) {
            long total = ((Number) r[2]).longValue();
            long promoters = ((Number) r[4]).longValue();
            long detractors = ((Number) r[5]).longValue();
            double enps = total == 0 ? 0 : ((promoters - detractors) * 100.0) / total;
            Map<String, Object> bucket = new LinkedHashMap<>();
            bucket.put("id", r[0] == null ? null : r[0].toString());
            bucket.put("label", r[1]);
            bucket.put("responses", total);
            bucket.put("averageScore", ((Number) r[3]).doubleValue());
            bucket.put("enps", java.math.BigDecimal.valueOf(enps)
                    .setScale(2, java.math.RoundingMode.HALF_UP));
            out.add(bucket);
        }
        return out;
    }

    private List<Map<String, Object>> rowsToBuckets(List<Object[]> rows) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] r : rows) {
            long total = ((Number) r[1]).longValue();
            long promoters = ((Number) r[3]).longValue();
            long detractors = ((Number) r[4]).longValue();
            double enps = total == 0 ? 0 : ((promoters - detractors) * 100.0) / total;
            Map<String, Object> bucket = new LinkedHashMap<>();
            bucket.put("band", r[0]);
            bucket.put("responses", total);
            bucket.put("averageScore", ((Number) r[2]).doubleValue());
            bucket.put("enps", java.math.BigDecimal.valueOf(enps)
                    .setScale(2, java.math.RoundingMode.HALF_UP));
            out.add(bucket);
        }
        return out;
    }
}
