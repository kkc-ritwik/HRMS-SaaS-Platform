package com.hrms.performance.service;

import com.hrms.performance.dto.PerformanceAnalyticsDto;
import com.hrms.performance.entity.Review;
import com.hrms.performance.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PerformanceAnalyticsService {

    private final ReviewRepository   reviewRepository;
    private final ReviewCycleService cycleService;

    // ── 9-box grid ────────────────────────────────────────────────────────────

    /**
     * Builds the 9-box grid from FINALIZED MANAGER reviews in a cycle.
     * X-axis (performance): mapped from performanceRating (1-5) → band 1-3
     * Y-axis (potential):   mapped from potentialRating  (1-5) → band 1-3
     */
    @Transactional(readOnly = true)
    public PerformanceAnalyticsDto.NineBoxGrid getNineBoxGrid(String tenantId, UUID cycleId) {
        var cycle = cycleService.getEntity(tenantId, cycleId);

        List<Review> reviews = reviewRepository.findByTenantIdAndCycleIdAndReviewTypeAndStatusAndDeletedFalse(
                tenantId, cycleId, Review.ReviewType.MANAGER, Review.ReviewStatus.FINALIZED);

        // Group employees into 9-box cells
        Map<String, List<PerformanceAnalyticsDto.EmployeePosition>> cellMap = new LinkedHashMap<>();

        for (Review r : reviews) {
            if (r.getPerformanceRating() == null || r.getPotentialRating() == null) continue;

            int perfBand      = toBand(r.getPerformanceRating().doubleValue(), 5.0);
            int potentialBand = toBand(r.getPotentialRating(), 5);
            String key        = perfBand + ":" + potentialBand;

            cellMap.computeIfAbsent(key, k -> new ArrayList<>())
                   .add(PerformanceAnalyticsDto.EmployeePosition.builder()
                           .employeeId(r.getEmployeeId())
                           .performanceRating(r.getPerformanceRating())
                           .potentialRating(r.getPotentialRating())
                           .overallRating(r.getOverallRating())
                           .build());
        }

        List<PerformanceAnalyticsDto.NineBoxCell> grid = new ArrayList<>();
        for (int perf = 1; perf <= 3; perf++) {
            for (int pot = 1; pot <= 3; pot++) {
                String key  = perf + ":" + pot;
                List<PerformanceAnalyticsDto.EmployeePosition> employees =
                        cellMap.getOrDefault(key, List.of());
                grid.add(PerformanceAnalyticsDto.NineBoxCell.builder()
                        .performanceBand(perf)
                        .potentialBand(pot)
                        .label(nineBoxLabel(perf, pot))
                        .employees(employees)
                        .count(employees.size())
                        .build());
            }
        }

        return PerformanceAnalyticsDto.NineBoxGrid.builder()
                .cycleId(cycleId)
                .cycleName(cycle.getName())
                .grid(grid)
                .totalReviewed(reviews.size())
                .build();
    }

    // ── Team summary ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PerformanceAnalyticsDto.TeamSummary getTeamSummary(String tenantId,
                                                               UUID cycleId, UUID managerId) {
        List<Review> reviews = reviewRepository.findManagerReviews(tenantId, cycleId, managerId);

        List<PerformanceAnalyticsDto.TeamMemberSummary> members = reviews.stream().map(r -> {
            int perfBand = r.getPerformanceRating() != null
                    ? toBand(r.getPerformanceRating().doubleValue(), 5.0) : 0;
            int potBand  = r.getPotentialRating() != null
                    ? toBand(r.getPotentialRating(), 5) : 0;
            return PerformanceAnalyticsDto.TeamMemberSummary.builder()
                    .employeeId(r.getEmployeeId())
                    .overallRating(r.getOverallRating())
                    .goalScore(r.getGoalScore())
                    .competencyScore(r.getCompetencyScore())
                    .performanceRating(r.getPerformanceRating())
                    .potentialRating(r.getPotentialRating() != null ? r.getPotentialRating() : 0)
                    .performanceBand(bandLabel(perfBand))
                    .potentialBand(bandLabel(potBand))
                    .nineBoxLabel(perfBand > 0 && potBand > 0 ? nineBoxLabel(perfBand, potBand) : null)
                    .build();
        }).toList();

        BigDecimal avgOverall = avg(reviews.stream()
                .map(Review::getOverallRating).filter(Objects::nonNull).toList());
        BigDecimal avgGoal    = avg(reviews.stream()
                .map(Review::getGoalScore).filter(Objects::nonNull).toList());
        BigDecimal avgComp    = avg(reviews.stream()
                .map(Review::getCompetencyScore).filter(Objects::nonNull).toList());

        return PerformanceAnalyticsDto.TeamSummary.builder()
                .cycleId(cycleId).managerId(managerId).members(members)
                .avgOverallRating(avgOverall).avgGoalScore(avgGoal).avgCompetencyScore(avgComp)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Maps a 1-to-scale rating into a 1-3 band. */
    private int toBand(double rating, double scale) {
        if (rating <= scale / 3.0)        return 1; // LOW
        if (rating <= (scale * 2.0) / 3.0) return 2; // MEDIUM
        return 3;                                    // HIGH
    }

    private int toBand(int rating, int scale) {
        return toBand((double) rating, (double) scale);
    }

    private String bandLabel(int band) {
        return switch (band) {
            case 1 -> "LOW";
            case 2 -> "MEDIUM";
            case 3 -> "HIGH";
            default -> "N/A";
        };
    }

    private String nineBoxLabel(int perf, int pot) {
        // Classic 9-box labelling: perf=X, pot=Y
        if (perf == 3 && pot == 3) return "Star / Consistent Star";
        if (perf == 2 && pot == 3) return "Future Star / High Potential";
        if (perf == 1 && pot == 3) return "Enigma / Rough Diamond";
        if (perf == 3 && pot == 2) return "High Performer";
        if (perf == 2 && pot == 2) return "Core Player / Backbone";
        if (perf == 1 && pot == 2) return "Dilemma / Inconsistent Player";
        if (perf == 3 && pot == 1) return "Expert / Specialist";
        if (perf == 2 && pot == 1) return "Solid Professional";
        return "Under Performer / Needs Attention";  // 1,1
    }

    private BigDecimal avg(List<BigDecimal> values) {
        if (values.isEmpty()) return null;
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    // ── Org-wide rating analytics ───────────────────────────────────────────────

    private List<Review> finalizedManagerReviews(String tenantId, UUID cycleId) {
        return cycleId != null
                ? reviewRepository.findByTenantIdAndCycleIdAndReviewTypeAndStatusAndDeletedFalse(
                        tenantId, cycleId, Review.ReviewType.MANAGER, Review.ReviewStatus.FINALIZED)
                : reviewRepository.findByTenantIdAndReviewTypeAndStatusAndDeletedFalse(
                        tenantId, Review.ReviewType.MANAGER, Review.ReviewStatus.FINALIZED);
    }

    /** Distribution of overall ratings, bucketed 1–5, with counts + percentages. */
    @Transactional(readOnly = true)
    public Map<String, Object> getRatingDistribution(String tenantId, UUID cycleId) {
        List<Review> reviews = finalizedManagerReviews(tenantId, cycleId);
        Map<Integer, Long> counts = new TreeMap<>();
        for (int i = 1; i <= 5; i++) counts.put(i, 0L);
        for (Review r : reviews) {
            if (r.getOverallRating() == null) continue;
            int band = clamp(r.getOverallRating().setScale(0, RoundingMode.HALF_UP).intValue());
            counts.merge(band, 1L, Long::sum);
        }
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        List<Map<String, Object>> bands = new ArrayList<>();
        counts.forEach((rating, count) -> bands.add(Map.of(
                "rating", rating,
                "count", count,
                "percentage", total == 0 ? 0.0
                        : BigDecimal.valueOf(count * 100.0 / total).setScale(1, RoundingMode.HALF_UP).doubleValue())));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", total);
        out.put("bands", bands);
        return out;
    }

    /** Min / max / average / median / std-dev of overall ratings. */
    @Transactional(readOnly = true)
    public Map<String, Object> getRatingSpread(String tenantId, UUID cycleId) {
        List<Double> ratings = new ArrayList<>();
        for (Review r : finalizedManagerReviews(tenantId, cycleId)) {
            if (r.getOverallRating() != null) ratings.add(r.getOverallRating().doubleValue());
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("count", ratings.size());
        if (ratings.isEmpty()) {
            out.put("min", null); out.put("max", null); out.put("average", null);
            out.put("median", null); out.put("stdDev", null);
            return out;
        }
        Collections.sort(ratings);
        double sum = ratings.stream().mapToDouble(Double::doubleValue).sum();
        double mean = sum / ratings.size();
        double variance = ratings.stream().mapToDouble(v -> (v - mean) * (v - mean)).sum() / ratings.size();
        double median = ratings.size() % 2 == 1
                ? ratings.get(ratings.size() / 2)
                : (ratings.get(ratings.size() / 2 - 1) + ratings.get(ratings.size() / 2)) / 2.0;
        out.put("min", ratings.get(0));
        out.put("max", ratings.get(ratings.size() - 1));
        out.put("average", round1(mean));
        out.put("median", round1(median));
        out.put("stdDev", round1(Math.sqrt(variance)));
        return out;
    }

    /** Calibration matrix — counts of employees per performance-rating band. */
    @Transactional(readOnly = true)
    public Map<String, Object> getCalibrationMatrix(String tenantId, UUID cycleId) {
        List<Review> reviews = finalizedManagerReviews(tenantId, cycleId);
        Map<Integer, Long> perf = new TreeMap<>();
        for (int i = 1; i <= 5; i++) perf.put(i, 0L);
        for (Review r : reviews) {
            if (r.getPerformanceRating() == null) continue;
            int band = clamp(r.getPerformanceRating().setScale(0, RoundingMode.HALF_UP).intValue());
            perf.merge(band, 1L, Long::sum);
        }
        long total = perf.values().stream().mapToLong(Long::longValue).sum();
        List<Map<String, Object>> rows = new ArrayList<>();
        perf.forEach((rating, count) -> rows.add(Map.of(
                "performanceRating", rating,
                "count", count,
                "percentage", total == 0 ? 0.0
                        : BigDecimal.valueOf(count * 100.0 / total).setScale(1, RoundingMode.HALF_UP).doubleValue())));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", total);
        out.put("rows", rows);
        return out;
    }

    private int clamp(int v) { return Math.max(1, Math.min(5, v)); }

    private double round1(double v) {
        return BigDecimal.valueOf(v).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
