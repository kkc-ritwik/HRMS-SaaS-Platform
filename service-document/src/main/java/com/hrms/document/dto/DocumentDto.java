package com.hrms.document.dto;

import com.hrms.document.entity.Document.DocumentStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class DocumentDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotBlank
        private String fileName;

        private UUID employeeId;

        private UUID documentTypeId;

        private String fileUrl;

        private Long fileSizeBytes;

        private String mimeType;

        private LocalDate expiryDate;

        private String remarks;

        private DocumentStatus status;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String fileName;

        private UUID employeeId;

        private UUID documentTypeId;

        private String fileUrl;

        private Long fileSizeBytes;

        private String mimeType;

        private LocalDate expiryDate;

        private String remarks;

        private DocumentStatus status;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private UUID employeeId;
        private UUID documentTypeId;
        private String fileName;
        private String fileUrl;
        private Long fileSizeBytes;
        private String mimeType;
        private DocumentStatus status;
        private LocalDate expiryDate;
        private String remarks;
        private UUID uploadedBy;
        private String tenantId;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
