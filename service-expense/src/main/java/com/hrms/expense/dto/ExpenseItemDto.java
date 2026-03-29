package com.hrms.expense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class ExpenseItemDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull(message = "Report ID is required")
        private UUID reportId;

        private UUID categoryId;

        @NotBlank(message = "Description is required")
        private String description;

        @NotNull(message = "Amount is required")
        private BigDecimal amount;

        private String currency = "USD";

        @NotNull(message = "Expense date is required")
        private LocalDate expenseDate;

        private String receiptUrl;

        private String merchant;

        private String notes;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private UUID reportId;

        private UUID categoryId;

        private String description;

        private BigDecimal amount;

        private String currency;

        private LocalDate expenseDate;

        private String receiptUrl;

        private String merchant;

        private String notes;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID reportId;
        private UUID categoryId;
        private String description;
        private BigDecimal amount;
        private String currency;
        private LocalDate expenseDate;
        private String receiptUrl;
        private String merchant;
        private String notes;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
