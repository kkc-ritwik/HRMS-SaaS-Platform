package com.hrms.compensation.dto;

import com.hrms.compensation.entity.Benefit.BenefitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class BenefitDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotBlank
        private String name;

        @NotBlank
        private String code;

        private String description;

        @NotNull
        private BenefitType benefitType;

        private String provider;
        private BigDecimal coverageAmount;
        private BigDecimal employeeContribution;
        private BigDecimal employerContribution;
        private String currency = "USD";
        private boolean active = true;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String name;
        private String code;
        private String description;
        private BenefitType benefitType;
        private String provider;
        private BigDecimal coverageAmount;
        private BigDecimal employeeContribution;
        private BigDecimal employerContribution;
        private String currency;
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
        private BenefitType benefitType;
        private String provider;
        private BigDecimal coverageAmount;
        private BigDecimal employeeContribution;
        private BigDecimal employerContribution;
        private String currency;
        private boolean active;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
