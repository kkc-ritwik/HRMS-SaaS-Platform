package com.hrms.performance.repository;

import com.hrms.performance.entity.ReviewRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRatingRepository extends JpaRepository<ReviewRating, UUID> {

    Optional<ReviewRating> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    List<ReviewRating> findByReviewIdAndTenantIdAndDeletedFalse(UUID reviewId, String tenantId);

    List<ReviewRating> findByReviewIdAndRatingTypeAndTenantIdAndDeletedFalse(
            UUID reviewId, ReviewRating.RatingType ratingType, String tenantId);

    Optional<ReviewRating> findByReviewIdAndCompetencyIdAndTenantIdAndDeletedFalse(
            UUID reviewId, UUID competencyId, String tenantId);

    Optional<ReviewRating> findByReviewIdAndGoalIdAndTenantIdAndDeletedFalse(
            UUID reviewId, UUID goalId, String tenantId);
}
