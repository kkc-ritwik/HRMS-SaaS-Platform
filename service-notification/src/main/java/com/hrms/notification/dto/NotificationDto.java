package com.hrms.notification.dto;

import com.hrms.notification.entity.Notification.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class NotificationDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull(message = "Employee ID is required")
        private UUID employeeId;

        @NotBlank(message = "Title is required")
        private String title;

        private String message;

        @NotNull(message = "Notification type is required")
        private NotificationType notificationType;

        private String referenceType;

        private UUID referenceId;

        private Instant sentAt;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private Boolean read;

        private Instant readAt;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID employeeId;
        private String title;
        private String message;
        private NotificationType notificationType;
        private String referenceType;
        private UUID referenceId;
        private boolean read;
        private Instant readAt;
        private Instant sentAt;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
