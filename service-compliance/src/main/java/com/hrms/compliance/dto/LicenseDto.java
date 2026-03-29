package com.hrms.compliance.dto;

import com.hrms.compliance.entity.License.LicenseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class LicenseDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull(message = "Employee ID is required")
        private UUID employeeId;

        @NotBlank(message = "License type is required")
        private String licenseType;

        private String licenseNumber;

        private String issuingAuthority;

        private LocalDate issueDate;

        private LocalDate expiryDate;

        private Integer renewalReminderDays;

        private String documentUrl;

        private String notes;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String licenseType;

        private String licenseNumber;

        private String issuingAuthority;

        private LocalDate issueDate;

        private LocalDate expiryDate;

        private LicenseStatus status;

        private Integer renewalReminderDays;

        private String documentUrl;

        private String notes;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID employeeId;
        private String licenseType;
        private String licenseNumber;
        private String issuingAuthority;
        private LocalDate issueDate;
        private LocalDate expiryDate;
        private LicenseStatus status;
        private int renewalReminderDays;
        private String documentUrl;
        private String notes;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
