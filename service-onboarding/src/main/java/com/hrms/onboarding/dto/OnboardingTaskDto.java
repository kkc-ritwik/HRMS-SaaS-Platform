package com.hrms.onboarding.dto;

import com.hrms.onboarding.entity.OnboardingTask.TaskType;
import com.hrms.onboarding.entity.OnboardingTask.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class OnboardingTaskDto {

    @Getter
    @Setter
    public static class CreateRequest {

        private UUID templateId;

        private UUID employeeId;

        @NotBlank(message = "Title is required")
        private String title;

        private String description;

        private TaskType taskType;

        private String assignedToRole;

        private int dueDaysOffset = 0;

        private boolean required = true;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private UUID templateId;

        private UUID employeeId;

        private String title;

        private String description;

        private TaskType taskType;

        private String assignedToRole;

        private Integer dueDaysOffset;

        private Boolean required;

        private TaskStatus status;

        private Instant completedAt;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID templateId;
        private UUID employeeId;
        private String title;
        private String description;
        private TaskType taskType;
        private String assignedToRole;
        private int dueDaysOffset;
        private boolean required;
        private TaskStatus status;
        private Instant completedAt;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
