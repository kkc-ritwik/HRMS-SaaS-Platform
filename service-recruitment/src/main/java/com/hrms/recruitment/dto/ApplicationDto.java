package com.hrms.recruitment.dto;

import com.hrms.recruitment.entity.Application;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ApplicationDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotNull private UUID        requisitionId;
        @NotNull private UUID        candidateId;
        private BigDecimal           currentCtc;
        private BigDecimal           expectedCtc;
        private Integer              noticePeriodDays;
        private String               notes;
    }

    @Getter @Setter
    public static class MoveStageRequest {
        @NotNull private Application.ApplicationStage stage;
        private String               rejectionReason;
        private String               notes;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID                          id;
        private UUID                          requisitionId;
        private String                        requisitionTitle;
        private UUID                          candidateId;
        private CandidateDto.Summary          candidate;
        private Application.ApplicationStage  stage;
        private String                        rejectionReason;
        private Instant                       appliedAt;
        private Instant                       stageChangedAt;
        private BigDecimal                    currentCtc;
        private BigDecimal                    expectedCtc;
        private Integer                       noticePeriodDays;
        private String                        notes;
        private Instant                       createdAt;
        private Instant                       updatedAt;
    }

    /** Pipeline view: one row per stage with count. */
    @Getter @Setter @Builder
    public static class StageCount {
        private Application.ApplicationStage stage;
        private long                         count;
    }
}
