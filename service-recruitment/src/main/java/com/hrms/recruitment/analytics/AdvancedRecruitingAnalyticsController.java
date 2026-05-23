package com.hrms.recruitment.analytics;

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
 * Recruiting analytics beyond the existing pipeline / source dashboard:
 *   · Cost-per-hire (CPH)           — total recruitment spend / hires in period
 *   · Time-to-fill (TTF)            — requisition opened → first offer accepted
 *   · Offer acceptance rate         — accepted / offered
 *   · Quality-of-hire proxy         — first-year retention of new hires
 *   · Open req aging                — buckets: 0-30, 30-60, 60-90, 90+ days
 *
 * Cost inputs come from a side table `recruit_cost_entries` (agency fees, job board fees,
 * referral bonuses, sign-on bonuses) that the AR team enters manually or via integration.
 */
@RestController
@RequestMapping("/api/recruitment/analytics/advanced")
@RequiredArgsConstructor
public class AdvancedRecruitingAnalyticsController {

    @PersistenceContext private EntityManager em;

    @GetMapping("/cost-per-hire")
    @Transactional(readOnly = true)
    public Map<String, Object> costPerHire(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        String tenant = TenantContext.get();

        BigDecimal totalSpend = (BigDecimal) em.createNativeQuery(
                "SELECT COALESCE(SUM(amount), 0) FROM recruit_cost_entries " +
                "WHERE tenant_id = :t AND incurred_on BETWEEN :from AND :to AND is_deleted = false")
                .setParameter("t", tenant)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        long hires = ((Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM applications a " +
                "WHERE a.tenant_id = :t AND a.stage = 'HIRED' AND a.is_deleted = false " +
                "AND a.stage_changed_at::date BETWEEN :from AND :to")
                .setParameter("t", tenant)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult()).longValue();

        BigDecimal cph = hires == 0 ? BigDecimal.ZERO
                : totalSpend.divide(BigDecimal.valueOf(hires), 2, RoundingMode.HALF_UP);

        return Map.of(
                "from", from.toString(), "to", to.toString(),
                "totalSpend", totalSpend,
                "hires", hires,
                "costPerHire", cph
        );
    }

    @GetMapping("/time-to-fill")
    @Transactional(readOnly = true)
    public Map<String, Object> timeToFill(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        String tenant = TenantContext.get();

        Object[] row = (Object[]) em.createNativeQuery(
                "SELECT AVG(EXTRACT(EPOCH FROM (a.stage_changed_at - r.created_at)) / 86400)::numeric, " +
                "       COUNT(*) " +
                "FROM applications a " +
                "JOIN job_requisitions r ON r.id = a.requisition_id AND r.tenant_id = a.tenant_id " +
                "WHERE a.tenant_id = :t AND a.stage = 'HIRED' AND a.is_deleted = false " +
                "AND a.stage_changed_at::date BETWEEN :from AND :to")
                .setParameter("t", tenant)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        BigDecimal avg = row[0] == null ? BigDecimal.ZERO : (BigDecimal) row[0];
        return Map.of(
                "from", from.toString(), "to", to.toString(),
                "avgDaysToFill", avg.setScale(1, RoundingMode.HALF_UP),
                "hires", ((Number) row[1]).longValue()
        );
    }

    @GetMapping("/offer-acceptance-rate")
    @Transactional(readOnly = true)
    public Map<String, Object> offerAcceptance(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        String tenant = TenantContext.get();
        Object[] row = (Object[]) em.createNativeQuery(
                "SELECT " +
                "  COUNT(*) FILTER (WHERE status = 'ACCEPTED'), " +
                "  COUNT(*) FILTER (WHERE status = 'DECLINED'), " +
                "  COUNT(*) " +
                "FROM offer_letters " +
                "WHERE tenant_id = :t AND issued_at::date BETWEEN :from AND :to AND is_deleted = false")
                .setParameter("t", tenant)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        long accepted = ((Number) row[0]).longValue();
        long declined = ((Number) row[1]).longValue();
        long total = ((Number) row[2]).longValue();
        BigDecimal rate = total == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(accepted * 100.0 / total).setScale(2, RoundingMode.HALF_UP);

        return Map.of(
                "from", from.toString(), "to", to.toString(),
                "offersIssued", total,
                "accepted", accepted,
                "declined", declined,
                "acceptanceRate", rate
        );
    }

    @GetMapping("/req-aging")
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> requisitionAging() {
        String tenant = TenantContext.get();
        List<Object[]> rows = em.createNativeQuery(
                "SELECT " +
                "  CASE " +
                "    WHEN EXTRACT(EPOCH FROM (NOW() - created_at)) / 86400 < 30 THEN '0-30' " +
                "    WHEN EXTRACT(EPOCH FROM (NOW() - created_at)) / 86400 < 60 THEN '30-60' " +
                "    WHEN EXTRACT(EPOCH FROM (NOW() - created_at)) / 86400 < 90 THEN '60-90' " +
                "    ELSE '90+' END AS bucket, " +
                "  COUNT(*) " +
                "FROM job_requisitions " +
                "WHERE tenant_id = :t AND status = 'ACTIVE' AND is_deleted = false " +
                "GROUP BY bucket ORDER BY bucket")
                .setParameter("t", tenant)
                .getResultList();

        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] r : rows) {
            out.add(Map.of("bucket", r[0], "count", ((Number) r[1]).longValue()));
        }
        return out;
    }

    /** Quality-of-hire proxy: percentage of new hires from period N still active 365 days later. */
    @GetMapping("/quality-of-hire")
    @Transactional(readOnly = true)
    public Map<String, Object> qualityOfHire(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        String tenant = TenantContext.get();
        Object[] row = (Object[]) em.createNativeQuery(
                "SELECT " +
                "  COUNT(*) FILTER (WHERE e.exit_date IS NULL OR e.exit_date - e.hire_date >= 365), " +
                "  COUNT(*) " +
                "FROM employees e " +
                "WHERE e.tenant_id = :t AND e.hire_date BETWEEN :from AND :to")
                .setParameter("t", tenant)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        long retained = ((Number) row[0]).longValue();
        long cohort = ((Number) row[1]).longValue();
        BigDecimal rate = cohort == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(retained * 100.0 / cohort).setScale(2, RoundingMode.HALF_UP);

        return Map.of(
                "cohortFrom", from.toString(), "cohortTo", to.toString(),
                "cohortSize", cohort,
                "retainedOneYear", retained,
                "qualityOfHirePercent", rate
        );
    }
}
