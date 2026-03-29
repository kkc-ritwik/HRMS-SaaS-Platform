package com.hrms.performance.dto;

import com.hrms.performance.entity.Goal;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GoalDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotNull  private UUID              employeeId;
        private UUID                        managerId;
        private UUID                        cycleId;
        private UUID                        parentGoalId;
        @NotNull  private Goal.GoalType     goalType;
        @NotBlank private String            title;
        private String                      description;
        @DecimalMin("0") @DecimalMax("100")
        private BigDecimal                  weightage = BigDecimal.valueOf(100);
        private BigDecimal                  targetValue;
        private String                      unit;
        @NotNull  private Goal.Priority     priority;
        private LocalDate                   startDate;
        private LocalDate                   dueDate;
    }

    @Getter @Setter
    public static class UpdateRequest {
        private String                      title;
        private String                      description;
        private BigDecimal                  weightage;
        private BigDecimal                  targetValue;
        private String                      unit;
        private Goal.Priority               priority;
        private LocalDate                   startDate;
        private LocalDate                   dueDate;
        private Goal.GoalStatus             status;
    }

    @Getter @Setter
    public static class ProgressUpdateRequest {
        @NotNull @DecimalMin("0") @DecimalMax("100")
        private BigDecimal progressValue;
        private BigDecimal currentValue;
        private String     comment;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID              id;
        private UUID              employeeId;
        private UUID              managerId;
        private UUID              cycleId;
        private UUID              parentGoalId;
        private Goal.GoalType     goalType;
        private String            title;
        private String            description;
        private BigDecimal        weightage;
        private BigDecimal        targetValue;
        private BigDecimal        currentValue;
        private String            unit;
        private BigDecimal        progress;
        private Goal.GoalStatus   status;
        private Goal.Priority     priority;
        private LocalDate         startDate;
        private LocalDate         dueDate;
        private List<Response>    keyResults;  // populated for OBJECTIVE type
        private Instant           createdAt;
        private Instant           updatedAt;
    }

    @Getter @Setter @Builder
    public static class UpdateResponse {
        private UUID       id;
        private UUID       goalId;
        private BigDecimal progressValue;
        private BigDecimal currentValue;
        private String     comment;
        private Instant    createdAt;
    }
}
