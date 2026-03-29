package com.hrms.notification.dto;

import com.hrms.notification.entity.NotificationPreference.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class NotificationPreferenceDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull(message = "Employee ID is required")
        private UUID employeeId;

        @NotNull(message = "Notification type is required")
        private NotificationType notificationType;

        private boolean emailEnabled = true;

        private boolean pushEnabled = true;

        private boolean inAppEnabled = true;

        private boolean smsEnabled = false;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private Boolean emailEnabled;

        private Boolean pushEnabled;

        private Boolean inAppEnabled;

        private Boolean smsEnabled;
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
        private NotificationType notificationType;
        private boolean emailEnabled;
        private boolean pushEnabled;
        private boolean inAppEnabled;
        private boolean smsEnabled;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
