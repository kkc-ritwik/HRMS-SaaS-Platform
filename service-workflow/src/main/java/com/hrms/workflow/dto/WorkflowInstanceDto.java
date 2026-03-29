package com.hrms.workflow.dto;

import com.hrms.workflow.entity.WorkflowInstance.InstanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class WorkflowInstanceDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull
        private UUID workflowId;

        @NotBlank
        private String entityType;

        @NotNull
        private UUID entityId;

        @NotNull
        private UUID initiatedBy;

        private List<String> contextData;

        private String notes;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private InstanceStatus status;

        private Integer currentStepOrder;

        private String notes;

        private Instant completedAt;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID workflowId;
        private String entityType;
        private UUID entityId;
        private UUID initiatedBy;
        private int currentStepOrder;
        private InstanceStatus status;
        private List<String> contextData;
        private Instant completedAt;
        private String notes;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
