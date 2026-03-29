package com.hrms.lms.dto;

import com.hrms.lms.entity.Certification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class CertificationDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull(message = "Employee ID is required")
        private UUID employeeId;

        private UUID courseId;

        @NotBlank(message = "Certificate name is required")
        private String certificateName;

        private String issuedBy;

        @NotNull(message = "Issue date is required")
        private LocalDate issueDate;

        private LocalDate expiryDate;
        private String certificateUrl;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private UUID employeeId;
        private UUID courseId;
        private String certificateName;
        private String issuedBy;
        private LocalDate issueDate;
        private LocalDate expiryDate;
        private String certificateUrl;
        private Certification.CertificationStatus status;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID employeeId;
        private UUID courseId;
        private String certificateName;
        private String issuedBy;
        private LocalDate issueDate;
        private LocalDate expiryDate;
        private String certificateUrl;
        private Certification.CertificationStatus status;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
