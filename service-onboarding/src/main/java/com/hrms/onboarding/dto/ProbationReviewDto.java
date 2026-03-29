package com.hrms.onboarding.dto;

import com.hrms.onboarding.entity.ProbationReview.ReviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class ProbationReviewDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull(message = "Employee ID is required")
        private UUID employeeId;

        private UUID reviewerId;

        private LocalDate reviewDate;

        private int periodMonths = 3;

        private String comments;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private UUID employeeId;

        private UUID reviewerId;

        private LocalDate reviewDate;

        private Integer periodMonths;

        private ReviewStatus status;

        private String overallRating;

        private String comments;

        private LocalDate extendedUntil;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID employeeId;
        private UUID reviewerId;
        private LocalDate reviewDate;
        private int periodMonths;
        private ReviewStatus status;
        private String overallRating;
        private String comments;
        private LocalDate extendedUntil;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
