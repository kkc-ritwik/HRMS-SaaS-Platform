package com.hrms.engagement.wellness;

import com.hrms.security.model.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/engagement/wellness")
@RequiredArgsConstructor
public class WellnessController {

    public interface ProgramRepo extends JpaRepository<WellnessProgram, UUID> {
        @Query("SELECT p FROM WellnessProgram p WHERE p.tenantId = :t AND p.active = true")
        List<WellnessProgram> active(@Param("t") String tenant);
    }

    public interface LogRepo extends JpaRepository<WellnessActivityLog, UUID> {
        @Query("SELECT l FROM WellnessActivityLog l WHERE l.tenantId = :t AND l.employeeId = :e AND l.programId = :p " +
                "AND l.logDate BETWEEN :from AND :to ORDER BY l.logDate")
        List<WellnessActivityLog> history(@Param("t") String tenant, @Param("e") UUID emp,
                                          @Param("p") UUID program,
                                          @Param("from") LocalDate from, @Param("to") LocalDate to);
    }

    private final ProgramRepo programs;
    private final LogRepo logs;
    @PersistenceContext private EntityManager em;

    @GetMapping("/programs")
    public List<WellnessProgram> listPrograms() { return programs.active(TenantContext.get()); }

    @PostMapping("/programs")
    public WellnessProgram createProgram(@RequestBody WellnessProgram p) {
        p.setTenantId(TenantContext.get());
        if (p.getActive() == null) p.setActive(true);
        return programs.save(p);
    }

    @PostMapping("/logs")
    @Transactional
    public WellnessActivityLog log(@RequestBody WellnessActivityLog in) {
        in.setTenantId(TenantContext.get());
        WellnessProgram program = programs.findById(in.getProgramId()).orElseThrow();
        int earned = 0;
        if (program.getPointsPerUnit() != null) {
            earned = (int) Math.min(
                    in.getMetricValue() * program.getPointsPerUnit(),
                    program.getMaxPointsPerDay() == null ? Long.MAX_VALUE : program.getMaxPointsPerDay());
        }
        in.setPointsEarned(earned);
        return logs.save(in);
    }

    @GetMapping("/logs")
    public List<WellnessActivityLog> history(@RequestParam UUID employeeId,
                                             @RequestParam UUID programId,
                                             @RequestParam LocalDate from,
                                             @RequestParam LocalDate to) {
        return logs.history(TenantContext.get(), employeeId, programId, from, to);
    }

    /** Top participants by total metric in a date window. */
    @GetMapping("/leaderboard")
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> leaderboard(@RequestParam UUID programId,
                                                 @RequestParam LocalDate from,
                                                 @RequestParam LocalDate to,
                                                 @RequestParam(defaultValue = "20") int limit) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT employee_id, SUM(metric_value), SUM(points_earned), COUNT(*) " +
                "FROM engagement_wellness_logs " +
                "WHERE tenant_id = :t AND program_id = :p AND log_date BETWEEN :from AND :to AND is_deleted = false " +
                "GROUP BY employee_id ORDER BY SUM(metric_value) DESC LIMIT :lim")
                .setParameter("t", TenantContext.get())
                .setParameter("p", programId)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("lim", limit)
                .getResultList();

        List<Map<String, Object>> out = new ArrayList<>();
        int rank = 1;
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("rank", rank++);
            m.put("employeeId", r[0].toString());
            m.put("totalMetric", ((Number) r[1]).longValue());
            m.put("pointsEarned", ((Number) r[2]).longValue());
            m.put("daysLogged", ((Number) r[3]).longValue());
            out.add(m);
        }
        return out;
    }
}
