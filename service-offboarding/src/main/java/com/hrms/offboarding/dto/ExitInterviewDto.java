package com.hrms.offboarding.dto;

import com.hrms.offboarding.entity.ExitInterview.InterviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class ExitInterviewDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull(message = "Separation ID is required")
        private UUID separationId;

        private UUID interviewerId;

        private Instant scheduledAt;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private UUID interviewerId;

        private Instant scheduledAt;

        private Instant completedAt;

        private Integer satisfactionRating;

        private String reasonForLeaving;

        private Boolean wouldRejoin;

        private String feedback;

        private InterviewStatus status;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID separationId;
        private UUID interviewerId;
        private Instant scheduledAt;
        private Instant completedAt;
        private Integer satisfactionRating;
        private String reasonForLeaving;
        private Boolean wouldRejoin;
        private String feedback;
        private InterviewStatus status;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
