package com.hrms.compensation.dto;

import com.hrms.compensation.entity.CompensationPlan.PlanStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class CompensationPlanDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull
        private UUID employeeId;

        private UUID payGradeId;

        @NotNull
        private BigDecimal baseSalary;

        private String currency = "USD";

        @NotNull
        private LocalDate effectiveDate;

        private LocalDate endDate;
        private BigDecimal allowances;
        private BigDecimal bonusPercentage;
        private String notes;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private UUID payGradeId;
        private BigDecimal baseSalary;
        private String currency;
        private LocalDate effectiveDate;
        private LocalDate endDate;
        private BigDecimal allowances;
        private BigDecimal bonusPercentage;
        private String notes;
        private PlanStatus status;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID employeeId;
        private UUID payGradeId;
        private BigDecimal baseSalary;
        private String currency;
        private LocalDate effectiveDate;
        private LocalDate endDate;
        private BigDecimal allowances;
        private BigDecimal bonusPercentage;
        private String notes;
        private PlanStatus status;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
