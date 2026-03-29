package com.hrms.expense.dto;

import com.hrms.expense.entity.ExpenseReport.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class ExpenseReportDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull(message = "Employee ID is required")
        private UUID employeeId;

        @NotBlank(message = "Title is required")
        private String title;

        private String description;

        private LocalDate periodStart;

        private LocalDate periodEnd;

        private String currency = "USD";
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private UUID employeeId;

        private String title;

        private String description;

        private LocalDate periodStart;

        private LocalDate periodEnd;

        private BigDecimal totalAmount;

        private String currency;

        private ReportStatus status;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID employeeId;
        private String title;
        private String description;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private BigDecimal totalAmount;
        private String currency;
        private ReportStatus status;
        private Instant submittedAt;
        private UUID approvedBy;
        private Instant approvedAt;
        private String rejectedReason;
        private Instant paidAt;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
