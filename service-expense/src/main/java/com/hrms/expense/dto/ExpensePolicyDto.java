package com.hrms.expense.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ExpensePolicyDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotBlank(message = "Name is required")
        private String name;

        private String description;

        private UUID categoryId;

        private String employeeLevel;

        private BigDecimal maxAmount;

        private String currency = "USD";

        private boolean approvalRequired = true;

        private boolean active = true;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String name;

        private String description;

        private UUID categoryId;

        private String employeeLevel;

        private BigDecimal maxAmount;

        private String currency;

        private Boolean approvalRequired;

        private Boolean active;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private String name;
        private String description;
        private UUID categoryId;
        private String employeeLevel;
        private BigDecimal maxAmount;
        private String currency;
        private boolean approvalRequired;
        private boolean active;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
