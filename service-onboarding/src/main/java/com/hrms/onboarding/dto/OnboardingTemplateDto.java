package com.hrms.onboarding.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class OnboardingTemplateDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotBlank(message = "Name is required")
        private String name;

        private String description;

        private UUID roleId;

        private UUID departmentId;

        private boolean active = true;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String name;

        private String description;

        private UUID roleId;

        private UUID departmentId;

        private Boolean active;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        private UUID id;
        private String tenantId;
        private String name;
        private String description;
        private UUID roleId;
        private UUID departmentId;
        private boolean active;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
