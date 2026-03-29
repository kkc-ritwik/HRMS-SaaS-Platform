package com.hrms.compliance.dto;

import com.hrms.compliance.entity.ComplianceTask.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class ComplianceTaskDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull(message = "Compliance item ID is required")
        private UUID complianceItemId;

        @NotBlank(message = "Title is required")
        private String title;

        private String description;

        private UUID assigneeId;

        private LocalDate dueDate;

        private String notes;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String title;

        private String description;

        private UUID assigneeId;

        private LocalDate dueDate;

        private TaskStatus status;

        private Instant completedAt;

        private String evidenceUrl;

        private String notes;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID complianceItemId;
        private String title;
        private String description;
        private UUID assigneeId;
        private LocalDate dueDate;
        private TaskStatus status;
        private Instant completedAt;
        private String evidenceUrl;
        private String notes;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
