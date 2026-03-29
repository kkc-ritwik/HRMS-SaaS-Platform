package com.hrms.performance.dto;

import com.hrms.performance.entity.ImprovementArea;
import com.hrms.performance.entity.PipPlan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class PipPlanDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotNull  private UUID                  employeeId;
        @NotNull  private UUID                  managerId;
        private UUID                            hrManagerId;
        private UUID                            reviewCycleId;
        @NotBlank private String                title;
        private String                          reason;
        @NotNull  private LocalDate             startDate;
        @NotNull  private LocalDate             endDate;
        private List<ImprovementArea>           improvementAreas;
        private String                          supportProvided;
        @NotNull  private PipPlan.CheckInFrequency checkInFrequency;
    }

    @Getter @Setter
    public static class UpdateRequest {
        private String                          title;
        private String                          reason;
        private LocalDate                       endDate;
        private PipPlan.PipStatus               status;
        private List<ImprovementArea>           improvementAreas;
        private String                          supportProvided;
        private PipPlan.CheckInFrequency        checkInFrequency;
        private String                          outcomeNotes;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID                            id;
        private UUID                            employeeId;
        private UUID                            managerId;
        private UUID                            hrManagerId;
        private UUID                            reviewCycleId;
        private String                          title;
        private String                          reason;
        private LocalDate                       startDate;
        private LocalDate                       endDate;
        private PipPlan.PipStatus               status;
        private List<ImprovementArea>           improvementAreas;
        private String                          supportProvided;
        private PipPlan.CheckInFrequency        checkInFrequency;
        private String                          outcomeNotes;
        private Instant                         closedAt;
        private Instant                         createdAt;
        private Instant                         updatedAt;
    }
}
