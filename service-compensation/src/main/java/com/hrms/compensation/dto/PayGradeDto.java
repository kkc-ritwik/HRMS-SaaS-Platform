package com.hrms.compensation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PayGradeDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotBlank
        private String name;

        @NotBlank
        private String code;

        private String description;

        @NotNull
        private BigDecimal minSalary;

        @NotNull
        private BigDecimal maxSalary;

        private String currency = "USD";

        private int level = 1;

        private boolean active = true;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String name;
        private String code;
        private String description;
        private BigDecimal minSalary;
        private BigDecimal maxSalary;
        private String currency;
        private Integer level;
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
        private BigDecimal minSalary;
        private BigDecimal maxSalary;
        private String currency;
        private int level;
        private boolean active;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
