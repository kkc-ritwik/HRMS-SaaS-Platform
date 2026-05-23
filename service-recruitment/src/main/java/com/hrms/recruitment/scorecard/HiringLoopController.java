package com.hrms.recruitment.scorecard;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Hiring-loop + scorecard REST surface.
 *   POST   /api/recruitment/hiring-loops               — create a loop
 *   POST   /api/recruitment/hiring-loops/{id}/schedule — set the panel timeline
 *   POST   /api/recruitment/hiring-loops/{id}/complete — close after debrief
 *   POST   /api/recruitment/scorecards                 — panellist submits scorecard
 *   GET    /api/recruitment/scorecards/application/{appId}/summary — aggregate across panel
 *   GET    /api/recruitment/scorecard-templates
 */
@RestController
@RequestMapping("/api/recruitment")
@RequiredArgsConstructor
public class HiringLoopController {

    public interface TemplateRepo extends JpaRepository<InterviewScorecardTemplate, UUID> {
        @Query("SELECT t FROM InterviewScorecardTemplate t WHERE t.tenantId = :t AND t.active = true")
        List<InterviewScorecardTemplate> active(@Param("t") String tenant);
    }

    public interface ScorecardRepo extends JpaRepository<InterviewScorecard, UUID> {
        @Query("SELECT s FROM InterviewScorecard s WHERE s.tenantId = :t AND s.applicationId = :a")
        List<InterviewScorecard> forApplication(@Param("t") String tenant, @Param("a") UUID application);
    }

    public interface LoopRepo extends JpaRepository<HiringLoop, UUID> {}

    private final TemplateRepo templates;
    private final ScorecardRepo scorecards;
    private final LoopRepo loops;

    // ── Templates ─────────────────────────────────────────────────────────────────

    @GetMapping("/scorecard-templates")
    public List<InterviewScorecardTemplate> listTemplates() { return templates.active(TenantContext.get()); }

    @PostMapping("/scorecard-templates")
    public InterviewScorecardTemplate createTemplate(@RequestBody InterviewScorecardTemplate t) {
        t.setTenantId(TenantContext.get());
        if (t.getActive() == null) t.setActive(true);
        return templates.save(t);
    }

    // ── Hiring loop ───────────────────────────────────────────────────────────────

    @PostMapping("/hiring-loops")
    public HiringLoop createLoop(@RequestBody HiringLoop l) {
        l.setTenantId(TenantContext.get());
        return loops.save(l);
    }

    @PostMapping("/hiring-loops/{id}/schedule")
    @Transactional
    @SuppressWarnings("unchecked")
    public HiringLoop schedule(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        HiringLoop l = loops.findById(id).orElseThrow();
        l.setSchedule((List<Map<String, Object>>) body.get("schedule"));
        l.setStatus(HiringLoop.Status.SCHEDULED);
        return loops.save(l);
    }

    @PostMapping("/hiring-loops/{id}/complete")
    @Transactional
    @SuppressWarnings("unchecked")
    public HiringLoop complete(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        HiringLoop l = loops.findById(id).orElseThrow();
        l.setDebriefNotes((Map<String, Object>) body.get("debriefNotes"));
        if (body.get("outcome") != null) {
            l.setOutcome(HiringLoop.Outcome.valueOf((String) body.get("outcome")));
        }
        l.setStatus(HiringLoop.Status.COMPLETED);
        return loops.save(l);
    }

    // ── Scorecards ────────────────────────────────────────────────────────────────

    @PostMapping("/scorecards")
    @Transactional
    public InterviewScorecard submit(@RequestBody InterviewScorecard s) {
        s.setTenantId(TenantContext.get());
        s.setSubmittedAt(OffsetDateTime.now());
        s.setLocked(true);
        if (s.getCompetencyScores() != null) {
            int sum = 0, count = 0;
            for (Map<String, Object> c : s.getCompetencyScores()) {
                Object score = c.get("score");
                if (score instanceof Number n) { sum += n.intValue(); count++; }
            }
            if (count > 0) s.setOverallScore(sum / count);
        }
        return scorecards.save(s);
    }

    @GetMapping("/scorecards/application/{appId}/summary")
    public Map<String, Object> summary(@PathVariable UUID appId) {
        List<InterviewScorecard> cards = scorecards.forApplication(TenantContext.get(), appId);
        if (cards.isEmpty()) return Map.of("scorecards", 0);

        Map<InterviewScorecard.Recommendation, Long> recCounts = new EnumMap<>(InterviewScorecard.Recommendation.class);
        int sumOverall = 0;
        int withOverall = 0;
        for (InterviewScorecard c : cards) {
            if (c.getRecommendation() != null) {
                recCounts.merge(c.getRecommendation(), 1L, Long::sum);
            }
            if (c.getOverallScore() != null) {
                sumOverall += c.getOverallScore();
                withOverall++;
            }
        }
        BigDecimal mean = withOverall == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(sumOverall * 1.0 / withOverall).setScale(2, RoundingMode.HALF_UP);

        // Simple decision rule — strict consensus
        InterviewScorecard.Recommendation decision = decide(recCounts, cards.size());

        return Map.of(
                "scorecards", cards.size(),
                "meanOverall", mean,
                "recommendations", recCounts,
                "consensus", decision == null ? "INCONCLUSIVE" : decision.name()
        );
    }

    private static InterviewScorecard.Recommendation decide(
            Map<InterviewScorecard.Recommendation, Long> recCounts, int total) {
        long noHire = recCounts.getOrDefault(InterviewScorecard.Recommendation.NO_HIRE, 0L)
                + recCounts.getOrDefault(InterviewScorecard.Recommendation.STRONG_NO_HIRE, 0L);
        long hire = recCounts.getOrDefault(InterviewScorecard.Recommendation.HIRE, 0L)
                + recCounts.getOrDefault(InterviewScorecard.Recommendation.STRONG_HIRE, 0L);
        if (noHire >= 2 || recCounts.containsKey(InterviewScorecard.Recommendation.STRONG_NO_HIRE)) {
            return InterviewScorecard.Recommendation.NO_HIRE;
        }
        if (hire >= Math.ceil(total * 0.66)) return InterviewScorecard.Recommendation.HIRE;
        return InterviewScorecard.Recommendation.MIXED;
    }
}
