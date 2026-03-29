package com.hrms.performance.service;

import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.DuplicateResourceException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.performance.dto.ReviewDto;
import com.hrms.performance.entity.*;
import com.hrms.performance.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository       reviewRepository;
    private final ReviewRatingRepository ratingRepository;
    private final ReviewCycleService     cycleService;
    private final CompetencyService      competencyService;
    private final GoalService            goalService;

    // ── Create review assignment ───────────────────────────────────────────────

    @Transactional
    public ReviewDto.Response create(String tenantId, ReviewDto.CreateRequest req, String currentUser) {
        cycleService.getEntity(tenantId, req.getCycleId()); // verify cycle exists

        if (reviewRepository.findByTenantIdAndCycleIdAndEmployeeIdAndReviewerIdAndReviewTypeAndDeletedFalse(
                tenantId, req.getCycleId(), req.getEmployeeId(),
                req.getReviewerId(), req.getReviewType()).isPresent()) {
            throw new DuplicateResourceException("Review", "cycleId+employeeId+reviewerId+type",
                    req.getReviewType());
        }

        Review r = new Review();
        r.setTenantId(tenantId);
        r.setCycleId(req.getCycleId());
        r.setEmployeeId(req.getEmployeeId());
        r.setManagerId(req.getManagerId());
        r.setReviewerId(req.getReviewerId());
        r.setReviewType(req.getReviewType());
        r.setStatus(Review.ReviewStatus.DRAFT);
        r.setCreatedBy(currentUser);
        return toResponse(reviewRepository.save(r));
    }

    // ── Save draft / submit ────────────────────────────────────────────────────

    @Transactional
    public ReviewDto.Response saveOrSubmit(String tenantId, UUID reviewId,
                                            ReviewDto.SubmitRequest req, String currentUser) {
        Review r = getEntity(tenantId, reviewId);
        if (r.getStatus() == Review.ReviewStatus.ACKNOWLEDGED ||
            r.getStatus() == Review.ReviewStatus.FINALIZED) {
            throw new BusinessException("REVIEW_LOCKED", "Review is already " + r.getStatus());
        }

        r.setStrengths(req.getStrengths());
        r.setDevelopmentAreas(req.getDevelopmentAreas());
        r.setManagerComments(req.getManagerComments());
        r.setFinalComments(req.getFinalComments());
        if (req.getOverallRating() != null) r.setOverallRating(req.getOverallRating());
        if (req.getPotentialRating() != null) r.setPotentialRating(req.getPotentialRating());
        r.setUpdatedBy(currentUser);

        // Upsert individual ratings
        if (req.getRatings() != null) {
            for (ReviewDto.RatingRequest rr : req.getRatings()) {
                upsertRating(tenantId, reviewId, rr, currentUser);
            }
            recomputeScores(tenantId, r);
        }

        if (req.isSubmit()) {
            r.setStatus(Review.ReviewStatus.SUBMITTED);
            r.setSubmittedAt(Instant.now());
        }

        return toResponse(reviewRepository.save(r));
    }

    // ── Acknowledge (employee acknowledges manager review) ────────────────────

    @Transactional
    public ReviewDto.Response acknowledge(String tenantId, UUID reviewId, String currentUser) {
        Review r = getEntity(tenantId, reviewId);
        if (r.getStatus() != Review.ReviewStatus.SUBMITTED) {
            throw new BusinessException("INVALID_STATUS",
                    "Only SUBMITTED reviews can be acknowledged. Current: " + r.getStatus());
        }
        r.setStatus(Review.ReviewStatus.ACKNOWLEDGED);
        r.setAcknowledgedAt(Instant.now());
        r.setUpdatedBy(currentUser);
        return toResponse(reviewRepository.save(r));
    }

    // ── Calibrate ─────────────────────────────────────────────────────────────

    @Transactional
    public ReviewDto.Response calibrate(String tenantId, UUID reviewId,
                                         ReviewDto.CalibrateRequest req, String currentUser) {
        Review r = getEntity(tenantId, reviewId);
        if (req.getOverallRating() != null)     r.setOverallRating(req.getOverallRating());
        if (req.getPerformanceRating() != null) r.setPerformanceRating(req.getPerformanceRating());
        if (req.getPotentialRating() != null)   r.setPotentialRating(req.getPotentialRating());
        if (req.getFinalComments() != null)     r.setFinalComments(req.getFinalComments());
        r.setStatus(Review.ReviewStatus.FINALIZED);
        r.setUpdatedBy(currentUser);
        return toResponse(reviewRepository.save(r));
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ReviewDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<ReviewDto.Response> listByCycle(String tenantId, UUID cycleId) {
        return reviewRepository.findByTenantIdAndCycleIdAndDeletedFalse(tenantId, cycleId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewDto.Response> listForEmployee(String tenantId, UUID employeeId) {
        return reviewRepository.findByTenantIdAndEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(
                        tenantId, employeeId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewDto.Response> myPendingReviews(String tenantId, UUID reviewerId) {
        return reviewRepository.findByTenantIdAndReviewerIdAndStatusAndDeletedFalse(
                        tenantId, reviewerId, Review.ReviewStatus.DRAFT)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewDto.Response> teamReviews(String tenantId, UUID cycleId, UUID managerId) {
        return reviewRepository.findManagerReviews(tenantId, cycleId, managerId)
                .stream().map(this::toResponse).toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    Review getEntity(String tenantId, UUID id) {
        return reviewRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
    }

    private void upsertRating(String tenantId, UUID reviewId,
                               ReviewDto.RatingRequest rr, String currentUser) {
        ReviewRating existing = null;
        if (rr.getCompetencyId() != null) {
            existing = ratingRepository.findByReviewIdAndCompetencyIdAndTenantIdAndDeletedFalse(
                    reviewId, rr.getCompetencyId(), tenantId).orElse(null);
        } else if (rr.getGoalId() != null) {
            existing = ratingRepository.findByReviewIdAndGoalIdAndTenantIdAndDeletedFalse(
                    reviewId, rr.getGoalId(), tenantId).orElse(null);
        }

        if (existing != null) {
            existing.setRating(rr.getRating());
            existing.setComments(rr.getComments());
            existing.setUpdatedBy(currentUser);
            ratingRepository.save(existing);
        } else {
            ReviewRating rating = new ReviewRating();
            rating.setTenantId(tenantId);
            rating.setReviewId(reviewId);
            rating.setCompetencyId(rr.getCompetencyId());
            rating.setGoalId(rr.getGoalId());
            rating.setRatingType(rr.getRatingType());
            rating.setRating(rr.getRating());
            rating.setComments(rr.getComments());
            rating.setCreatedBy(currentUser);
            ratingRepository.save(rating);
        }
    }

    private void recomputeScores(String tenantId, Review r) {
        List<ReviewRating> ratings = ratingRepository.findByReviewIdAndTenantIdAndDeletedFalse(
                r.getId(), tenantId);

        List<ReviewRating> competencyRatings = ratings.stream()
                .filter(rt -> rt.getRatingType() == ReviewRating.RatingType.COMPETENCY).toList();
        List<ReviewRating> goalRatings = ratings.stream()
                .filter(rt -> rt.getRatingType() == ReviewRating.RatingType.GOAL).toList();

        if (!competencyRatings.isEmpty()) {
            BigDecimal avg = average(competencyRatings);
            r.setCompetencyScore(avg);
        }
        if (!goalRatings.isEmpty()) {
            BigDecimal avg = average(goalRatings);
            r.setGoalScore(avg);
        }

        // Weighted overall: 60% goals, 40% competencies (if both present)
        BigDecimal gs = r.getGoalScore();
        BigDecimal cs = r.getCompetencyScore();
        if (gs != null && cs != null) {
            r.setPerformanceRating(
                    gs.multiply(BigDecimal.valueOf(0.6))
                      .add(cs.multiply(BigDecimal.valueOf(0.4)))
                      .setScale(2, RoundingMode.HALF_UP));
        } else if (gs != null) {
            r.setPerformanceRating(gs);
        } else if (cs != null) {
            r.setPerformanceRating(cs);
        }
    }

    private BigDecimal average(List<ReviewRating> ratings) {
        return ratings.stream()
                .map(ReviewRating::getRating)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(ratings.size()), 2, RoundingMode.HALF_UP);
    }

    private ReviewDto.Response toResponse(Review r) {
        String cycleName = null;
        try { cycleName = cycleService.getEntity(r.getTenantId(), r.getCycleId()).getName(); }
        catch (Exception ignored) {}

        List<ReviewDto.RatingResponse> ratings = ratingRepository
                .findByReviewIdAndTenantIdAndDeletedFalse(r.getId(), r.getTenantId())
                .stream().map(rt -> {
                    String compName = null;
                    String goalTitle = null;
                    if (rt.getCompetencyId() != null) {
                        try { compName = competencyService.getEntity(r.getTenantId(), rt.getCompetencyId()).getName(); }
                        catch (Exception ignored) {}
                    }
                    if (rt.getGoalId() != null) {
                        try { goalTitle = goalService.getEntity(r.getTenantId(), rt.getGoalId()).getTitle(); }
                        catch (Exception ignored) {}
                    }
                    return ReviewDto.RatingResponse.builder()
                            .id(rt.getId()).competencyId(rt.getCompetencyId()).competencyName(compName)
                            .goalId(rt.getGoalId()).goalTitle(goalTitle)
                            .ratingType(rt.getRatingType()).rating(rt.getRating()).comments(rt.getComments())
                            .build();
                }).toList();

        return ReviewDto.Response.builder()
                .id(r.getId()).cycleId(r.getCycleId()).cycleName(cycleName)
                .employeeId(r.getEmployeeId()).managerId(r.getManagerId()).reviewerId(r.getReviewerId())
                .reviewType(r.getReviewType()).status(r.getStatus())
                .overallRating(r.getOverallRating()).potentialRating(r.getPotentialRating())
                .performanceRating(r.getPerformanceRating())
                .goalScore(r.getGoalScore()).competencyScore(r.getCompetencyScore())
                .strengths(r.getStrengths()).developmentAreas(r.getDevelopmentAreas())
                .managerComments(r.getManagerComments()).finalComments(r.getFinalComments())
                .ratings(ratings)
                .submittedAt(r.getSubmittedAt()).acknowledgedAt(r.getAcknowledgedAt())
                .createdAt(r.getCreatedAt()).updatedAt(r.getUpdatedAt())
                .build();
    }
}
