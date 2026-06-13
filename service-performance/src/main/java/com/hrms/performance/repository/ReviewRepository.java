package com.hrms.performance.repository;

import com.hrms.performance.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Optional<Review> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    List<Review> findByTenantIdAndCycleIdAndDeletedFalse(String tenantId, UUID cycleId);

    List<Review> findByTenantIdAndEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(
            String tenantId, UUID employeeId);

    List<Review> findByTenantIdAndCycleIdAndEmployeeIdAndDeletedFalse(
            String tenantId, UUID cycleId, UUID employeeId);

    Optional<Review> findByTenantIdAndCycleIdAndEmployeeIdAndReviewerIdAndReviewTypeAndDeletedFalse(
            String tenantId, UUID cycleId, UUID employeeId, UUID reviewerId,
            Review.ReviewType reviewType);

    /** Reviewer's pending reviews (assigned but not yet submitted). */
    List<Review> findByTenantIdAndReviewerIdAndStatusAndDeletedFalse(
            String tenantId, UUID reviewerId, Review.ReviewStatus status);

    /** Manager's direct reports reviews in a cycle. */
    @Query("SELECT r FROM Review r WHERE r.tenantId = :tenantId AND r.cycleId = :cycleId " +
           "AND r.managerId = :managerId AND r.reviewType = 'MANAGER' AND r.deleted = false")
    List<Review> findManagerReviews(@Param("tenantId")  String tenantId,
                                    @Param("cycleId")   UUID cycleId,
                                    @Param("managerId") UUID managerId);

    /** All FINALIZED manager reviews in a cycle — for 9-box grid. */
    List<Review> findByTenantIdAndCycleIdAndReviewTypeAndStatusAndDeletedFalse(
            String tenantId, UUID cycleId, Review.ReviewType reviewType, Review.ReviewStatus status);

    /** All FINALIZED manager reviews across all cycles — for org-wide rating analytics. */
    List<Review> findByTenantIdAndReviewTypeAndStatusAndDeletedFalse(
            String tenantId, Review.ReviewType reviewType, Review.ReviewStatus status);
}
