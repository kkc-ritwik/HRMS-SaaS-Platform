package com.hrms.performance.dto;

import com.hrms.performance.entity.Feedback;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class FeedbackDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotNull  private UUID                   toEmployeeId;
        @NotNull  private Feedback.FeedbackType  feedbackType;
        @NotNull  private Feedback.Visibility    visibility;
        private UUID                             cycleId;
        private String                           context;
        @NotBlank private String                 message;
        private List<String>                     tags;
        private boolean                          anonymous = false;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID                      id;
        private UUID                      fromEmployeeId;
        private String                    fromEmployeeName;  // masked if anonymous
        private UUID                      toEmployeeId;
        private Feedback.FeedbackType     feedbackType;
        private Feedback.Visibility       visibility;
        private UUID                      cycleId;
        private String                    context;
        private String                    message;
        private List<String>              tags;
        private boolean                   anonymous;
        private Instant                   createdAt;
    }

    @Getter @Setter @Builder
    public static class Summary {
        private UUID                      id;
        private UUID                      fromEmployeeId;
        private String                    fromEmployeeName;
        private Feedback.FeedbackType     feedbackType;
        private String                    message;
        private List<String>              tags;
        private Instant                   createdAt;
    }
}
