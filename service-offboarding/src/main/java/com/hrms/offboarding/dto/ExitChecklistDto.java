package com.hrms.offboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class ExitChecklistDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull(message = "Separation ID is required")
        private UUID separationId;

        @NotBlank(message = "Task title is required")
        private String taskTitle;

        private String taskCategory;

        private UUID assignedTo;

        private LocalDate dueDate;

        private String notes;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String taskTitle;

        private String taskCategory;

        private UUID assignedTo;

        private LocalDate dueDate;

        private String notes;

        private Boolean completed;

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
        private UUID separationId;
        private String taskTitle;
        private String taskCategory;
        private UUID assignedTo;
        private LocalDate dueDate;
        private boolean completed;
        private Instant completedAt;
        private String notes;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
