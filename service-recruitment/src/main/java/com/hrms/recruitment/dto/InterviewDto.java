package com.hrms.recruitment.dto;

import com.hrms.recruitment.entity.Interview;
import com.hrms.recruitment.entity.InterviewPanelist;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class InterviewDto {

    @Getter @Setter
    public static class ScheduleRequest {
        @NotNull private UUID                    applicationId;
        @NotNull private Interview.InterviewType interviewType;
        private int                              roundNumber = 1;
        @NotNull private Instant                 scheduledAt;
        private int                              durationMinutes = 60;
        @NotNull private Interview.InterviewMode mode;
        private String                           meetingLink;
        private String                           venue;
        private List<PanelistRequest>            panelists;
    }

    @Getter @Setter
    public static class PanelistRequest {
        @NotNull private UUID                      interviewerId;
        private InterviewPanelist.PanelistRole     role = InterviewPanelist.PanelistRole.PANELIST;
    }

    @Getter @Setter
    public static class FeedbackRequest {
        @NotNull @Min(1) @Max(5) private int      overallRating;
        @NotNull private Interview.Recommendation  recommendation;
        private String                             feedback;
    }

    @Getter @Setter
    public static class PanelistFeedbackRequest {
        @NotNull @Min(1) @Max(5) private int      rating;
        private String                             feedback;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID                        id;
        private UUID                        applicationId;
        private Interview.InterviewType     interviewType;
        private int                         roundNumber;
        private Instant                     scheduledAt;
        private int                         durationMinutes;
        private Interview.InterviewMode     mode;
        private String                      meetingLink;
        private String                      venue;
        private Interview.InterviewStatus   status;
        private Integer                     overallRating;
        private Interview.Recommendation    recommendation;
        private String                      feedback;
        private List<PanelistSummary>       panelists;
        private Instant                     createdAt;
        private Instant                     updatedAt;
    }

    @Getter @Setter @Builder
    public static class PanelistSummary {
        private UUID                          id;
        private UUID                          interviewerId;
        private InterviewPanelist.PanelistRole role;
        private Integer                       rating;
        private String                        feedback;
        private Instant                       submittedAt;
    }
}
