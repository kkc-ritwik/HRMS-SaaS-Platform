package com.hrms.workflow.dto;

import com.hrms.workflow.entity.WorkflowStep.ApproverType;
import com.hrms.workflow.entity.WorkflowStep.StepType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class WorkflowStepDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull
        private UUID workflowId;

        @NotNull
        private Integer stepOrder;

        @NotBlank
        private String stepName;

        private StepType stepType;

        private ApproverType approverType;

        private UUID approverId;

        private String approverRole;

        private boolean canDelegate = false;

        private int slaHours = 24;

        private UUID escalationTo;

        private boolean required = true;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private UUID workflowId;

        private Integer stepOrder;

        private String stepName;

        private StepType stepType;

        private ApproverType approverType;

        private UUID approverId;

        private String approverRole;

        private Boolean canDelegate;

        private Integer slaHours;

        private UUID escalationTo;

        private Boolean required;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID workflowId;
        private int stepOrder;
        private String stepName;
        private StepType stepType;
        private ApproverType approverType;
        private UUID approverId;
        private String approverRole;
        private boolean canDelegate;
        private int slaHours;
        private UUID escalationTo;
        private boolean required;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
