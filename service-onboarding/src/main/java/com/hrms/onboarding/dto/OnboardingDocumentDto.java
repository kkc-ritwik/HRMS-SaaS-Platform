package com.hrms.onboarding.dto;

import com.hrms.onboarding.entity.OnboardingDocument.DocumentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class OnboardingDocumentDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull(message = "Employee ID is required")
        private UUID employeeId;

        @NotBlank(message = "Document name is required")
        private String documentName;

        private String documentType;

        private String fileUrl;

        private DocumentStatus status;

        private String remarks;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private UUID employeeId;

        private String documentName;

        private String documentType;

        private String fileUrl;

        private DocumentStatus status;

        private String remarks;
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
        private String documentName;
        private String documentType;
        private String fileUrl;
        private DocumentStatus status;
        private String remarks;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
