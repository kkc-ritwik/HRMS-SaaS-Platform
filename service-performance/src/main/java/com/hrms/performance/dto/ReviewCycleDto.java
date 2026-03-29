package com.hrms.performance.dto;

import com.hrms.performance.entity.ReviewCycle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class ReviewCycleDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank private String name;
        private String           description;
        @NotNull private ReviewCycle.CycleType cycleType;
        private String           fiscalYear;
        @NotNull private LocalDate periodStart;
        @NotNull private LocalDate periodEnd;
        private LocalDate        selfReviewDeadline;
        private LocalDate        managerReviewDeadline;
        private LocalDate        calibrationDate;
        private boolean          includeGoalRating = true;
        private boolean          includeCompetencyRating = true;
        private boolean          include360Feedback = false;
        private int              ratingScale = 5;
    }

    @Getter @Setter
    public static class UpdateRequest {
        private String           name;
        private String           description;
        private LocalDate        periodStart;
        private LocalDate        periodEnd;
        private LocalDate        selfReviewDeadline;
        private LocalDate        managerReviewDeadline;
        private LocalDate        calibrationDate;
        private Boolean          includeGoalRating;
        private Boolean          includeCompetencyRating;
        private Boolean          include360Feedback;
        private Integer          ratingScale;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID                      id;
        private String                    name;
        private String                    description;
        private ReviewCycle.CycleType     cycleType;
        private String                    fiscalYear;
        private LocalDate                 periodStart;
        private LocalDate                 periodEnd;
        private ReviewCycle.CycleStatus   status;
        private LocalDate                 selfReviewDeadline;
        private LocalDate                 managerReviewDeadline;
        private LocalDate                 calibrationDate;
        private boolean                   includeGoalRating;
        private boolean                   includeCompetencyRating;
        private boolean                   include360Feedback;
        private int                       ratingScale;
        private Instant                   createdAt;
        private Instant                   updatedAt;
    }
}
