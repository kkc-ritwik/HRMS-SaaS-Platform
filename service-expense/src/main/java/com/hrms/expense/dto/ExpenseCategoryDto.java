package com.hrms.expense.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ExpenseCategoryDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Code is required")
        private String code;

        private String description;

        private BigDecimal maxAmount;

        private boolean requiresReceipt = true;

        private boolean active = true;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String name;

        private String code;

        private String description;

        private BigDecimal maxAmount;

        private Boolean requiresReceipt;

        private Boolean active;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private String name;
        private String code;
        private String description;
        private BigDecimal maxAmount;
        private boolean requiresReceipt;
        private boolean active;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
