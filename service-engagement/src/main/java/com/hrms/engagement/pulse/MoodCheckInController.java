package com.hrms.engagement.pulse;

import com.hrms.security.model.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Pulse check-in REST API.
 *   POST  /api/engagement/pulse                       — submit own check-in
 *   GET   /api/engagement/pulse/me?from&to            — own history
 *   GET   /api/engagement/pulse/aggregate?from&to     — tenant-wide rollup (HR / manager)
 *   GET   /api/engagement/pulse/trend?days=30         — daily average for trend chart
 */
@RestController
@RequestMapping("/api/engagement/pulse")
@RequiredArgsConstructor
public class MoodCheckInController {

    public interface Repo extends JpaRepository<MoodCheckIn, UUID> {
        @Query("SELECT m FROM MoodCheckIn m WHERE m.tenantId = :t AND m.employeeId = :e " +
                "AND m.checkInDate BETWEEN :from AND :to ORDER BY m.checkInDate DESC")
        List<MoodCheckIn> findOwn(@Param("t") String tenant, @Param("e") UUID emp,
                                  @Param("from") LocalDate from, @Param("to") LocalDate to);

        @Query("SELECT COUNT(m) FROM MoodCheckIn m WHERE m.tenantId = :t AND m.employeeId = :e AND m.checkInDate = :d")
        long existsForDay(@Param("t") String tenant, @Param("e") UUID emp, @Param("d") LocalDate d);
    }

    private final Repo repo;
    @PersistenceContext private EntityManager em;

    @PostMapping
    @Transactional
    public Map<String, Object> submit(@RequestBody Map<String, Object> body) {
        String tenant = TenantContext.get();
        UUID employee = UUID.fromString((String) body.get("employeeId"));
        Integer score = ((Number) body.get("score")).intValue();
        if (score < 1 || score > 5) throw new IllegalArgumentException("score must be 1..5");

        LocalDate day = body.get("checkInDate") != null
                ? LocalDate.parse((String) body.get("checkInDate"))
                : LocalDate.now();

        // One check-in per employee per day — keep latest.
        if (repo.existsForDay(tenant, employee, day) > 0) {
            return Map.of("status", "ALREADY_SUBMITTED", "date", day.toString());
        }

        MoodCheckIn m = new MoodCheckIn();
        m.setTenantId(tenant);
        m.setEmployeeId(employee);
        m.setCheckInDate(day);
        m.setScore(score);
        m.setComment((String) body.get("comment"));
        m.setTheme((String) body.get("theme"));
        m.setAnonymous(Boolean.TRUE.equals(body.get("anonymous")));
        m.setCategory(MoodCheckIn.Category.valueOf(
                (String) body.getOrDefault("category", "DAILY")));

        repo.save(m);
        return Map.of("id", m.getId(), "status", "RECORDED");
    }

    @GetMapping("/me")
    public List<MoodCheckIn> mine(@RequestParam UUID employeeId,
                                  @RequestParam LocalDate from,
                                  @RequestParam LocalDate to) {
        return repo.findOwn(TenantContext.get(), employeeId, from, to);
    }

    /** Aggregate eNPS-style: %promoters - %detractors. */
    @GetMapping("/aggregate")
    @SuppressWarnings("unchecked")
    public Map<String, Object> aggregate(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        String tenant = TenantContext.get();
        Object[] row = (Object[]) em.createNativeQuery(
                "SELECT COUNT(*)," +
                "       AVG(score)," +
                "       SUM(CASE WHEN score >= 4 THEN 1 ELSE 0 END)," +
                "       SUM(CASE WHEN score = 3 THEN 1 ELSE 0 END)," +
                "       SUM(CASE WHEN score <= 2 THEN 1 ELSE 0 END) " +
                "FROM engagement_mood_checkins " +
                "WHERE tenant_id = :t AND check_in_date BETWEEN :from AND :to AND is_deleted = false")
                .setParameter("t", tenant)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        long total = ((Number) row[0]).longValue();
        if (total == 0) return Map.of("total", 0, "enps", 0, "averageScore", 0);
        long promoters = ((Number) row[2]).longValue();
        long neutrals = ((Number) row[3]).longValue();
        long detractors = ((Number) row[4]).longValue();
        double enps = ((promoters - detractors) * 100.0) / total;
        return Map.of(
                "total", total,
                "averageScore", ((Number) row[1]).doubleValue(),
                "promoters", promoters,
                "neutrals", neutrals,
                "detractors", detractors,
                "enps", BigDecimal.valueOf(enps).setScale(2, java.math.RoundingMode.HALF_UP)
        );
    }

    /** Day-by-day mean score for trend charts (last N days). */
    @GetMapping("/trend")
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> trend(@RequestParam(defaultValue = "30") int days) {
        String tenant = TenantContext.get();
        List<Object[]> rows = em.createNativeQuery(
                "SELECT check_in_date, AVG(score), COUNT(*) " +
                "FROM engagement_mood_checkins " +
                "WHERE tenant_id = :t AND check_in_date >= :since AND is_deleted = false " +
                "GROUP BY check_in_date ORDER BY check_in_date")
                .setParameter("t", tenant)
                .setParameter("since", LocalDate.now().minusDays(days))
                .getResultList();

        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] r : rows) {
            out.add(Map.of(
                    "date", r[0].toString(),
                    "averageScore", ((Number) r[1]).doubleValue(),
                    "count", ((Number) r[2]).longValue()
            ));
        }
        return out;
    }
}
