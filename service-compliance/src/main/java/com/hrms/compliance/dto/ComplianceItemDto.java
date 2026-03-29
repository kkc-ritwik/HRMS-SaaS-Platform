package com.hrms.compliance.dto;

import com.hrms.compliance.entity.ComplianceItem.ComplianceStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class ComplianceItemDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotBlank(message = "Title is required")
        private String title;

        private String description;

        private String category;

        private String regulation;

        private String jurisdiction;

        private LocalDate dueDate;

        private boolean recurring = false;

        private String recurrencePeriod;

        private UUID ownerId;

        private String notes;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String title;

        private String description;

        private String category;

        private String regulation;

        private String jurisdiction;

        private LocalDate dueDate;

        private Boolean recurring;

        private String recurrencePeriod;

        private ComplianceStatus status;

        private UUID ownerId;

        private String notes;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private String title;
        private String description;
        private String category;
        private String regulation;
        private String jurisdiction;
        private LocalDate dueDate;
        private boolean recurring;
        private String recurrencePeriod;
        private ComplianceStatus status;
        private UUID ownerId;
        private String notes;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
