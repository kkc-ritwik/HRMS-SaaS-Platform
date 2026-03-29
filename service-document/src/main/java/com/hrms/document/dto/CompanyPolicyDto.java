package com.hrms.document.dto;

import com.hrms.document.entity.CompanyPolicy.PolicyStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class CompanyPolicyDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotBlank
        private String title;

        private String description;

        private String policyType;

        private String content;

        private String version;

        private LocalDate effectiveDate;

        private LocalDate expiryDate;

        private boolean requiresAcknowledgement;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String title;

        private String description;

        private String policyType;

        private String content;

        private String version;

        private LocalDate effectiveDate;

        private LocalDate expiryDate;

        private Boolean requiresAcknowledgement;

        private PolicyStatus status;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String title;
        private String description;
        private String policyType;
        private String content;
        private String version;
        private LocalDate effectiveDate;
        private LocalDate expiryDate;
        private PolicyStatus status;
        private boolean requiresAcknowledgement;
        private String tenantId;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
