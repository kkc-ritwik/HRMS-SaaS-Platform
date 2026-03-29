package com.hrms.compensation.dto;

import com.hrms.compensation.entity.EmployeeBenefit.BenefitStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class EmployeeBenefitDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull
        private UUID employeeId;

        @NotNull
        private UUID benefitId;

        @NotNull
        private LocalDate enrollmentDate;

        private String notes;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private LocalDate terminationDate;
        private BenefitStatus status;
        private String notes;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID employeeId;
        private UUID benefitId;
        private LocalDate enrollmentDate;
        private LocalDate terminationDate;
        private BenefitStatus status;
        private String notes;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
