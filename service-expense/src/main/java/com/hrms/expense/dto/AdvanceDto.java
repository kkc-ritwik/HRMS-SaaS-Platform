package com.hrms.expense.dto;

import com.hrms.expense.entity.Advance.AdvanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class AdvanceDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull(message = "Employee ID is required")
        private UUID employeeId;

        @NotNull(message = "Amount is required")
        private BigDecimal amount;

        private String currency = "USD";

        private String purpose;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private UUID employeeId;

        private BigDecimal amount;

        private String currency;

        private String purpose;

        private AdvanceStatus status;

        private UUID approvedBy;

        private Instant approvedAt;

        private Instant disbursedAt;

        private LocalDate dueDate;

        private BigDecimal settledAmount;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID employeeId;
        private BigDecimal amount;
        private String currency;
        private String purpose;
        private AdvanceStatus status;
        private Instant requestedAt;
        private UUID approvedBy;
        private Instant approvedAt;
        private Instant disbursedAt;
        private LocalDate dueDate;
        private BigDecimal settledAmount;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
