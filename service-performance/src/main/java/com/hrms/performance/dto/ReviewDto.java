package com.hrms.performance.dto;

import com.hrms.performance.entity.Review;
import com.hrms.performance.entity.ReviewRating;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ReviewDto {

    /** Create a self or peer review assignment. */
    @Getter @Setter
    public static class CreateRequest {
        @NotNull private UUID              cycleId;
        @NotNull private UUID              employeeId;
        private UUID                       managerId;
        @NotNull private UUID              reviewerId;
        @NotNull private Review.ReviewType reviewType;
    }

    /** Save / submit self or manager review content. */
    @Getter @Setter
    public static class SubmitRequest {
        private String                       strengths;
        private String                       developmentAreas;
        private String                       managerComments;
        private String                       finalComments;
        @DecimalMin("1") @DecimalMax("5")
        private BigDecimal                   overallRating;
        @Min(1) @Max(5)
        private Integer                      potentialRating;
        private List<RatingRequest>          ratings;
        /** If true, moves status from DRAFT → SUBMITTED. */
        private boolean                      submit = false;
    }

    @Getter @Setter
    public static class RatingRequest {
        private UUID                         competencyId;
        private UUID                         goalId;
        @NotNull private ReviewRating.RatingType ratingType;
        @NotNull @DecimalMin("0") @DecimalMax("5")
        private BigDecimal                   rating;
        private String                       comments;
    }

    /** Calibration: HR adjusts scores and finalises. */
    @Getter @Setter
    public static class CalibrateRequest {
        private BigDecimal                   overallRating;
        private BigDecimal                   performanceRating;
        @Min(1) @Max(5) private Integer      potentialRating;
        private String                       finalComments;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID                         id;
        private UUID                         cycleId;
        private String                       cycleName;
        private UUID                         employeeId;
        private UUID                         managerId;
        private UUID                         reviewerId;
        private Review.ReviewType            reviewType;
        private Review.ReviewStatus          status;
        private BigDecimal                   overallRating;
        private Integer                      potentialRating;
        private BigDecimal                   performanceRating;
        private BigDecimal                   goalScore;
        private BigDecimal                   competencyScore;
        private String                       strengths;
        private String                       developmentAreas;
        private String                       managerComments;
        private String                       finalComments;
        private List<RatingResponse>         ratings;
        private Instant                      submittedAt;
        private Instant                      acknowledgedAt;
        private Instant                      createdAt;
        private Instant                      updatedAt;
    }

    @Getter @Setter @Builder
    public static class RatingResponse {
        private UUID                         id;
        private UUID                         competencyId;
        private String                       competencyName;
        private UUID                         goalId;
        private String                       goalTitle;
        private ReviewRating.RatingType      ratingType;
        private BigDecimal                   rating;
        private String                       comments;
    }
}
