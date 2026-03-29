package com.hrms.asset.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class AssetCategoryDto {

    @Getter @Setter
    public static class CreateRequest {

        @NotBlank
        private String name;

        @NotBlank
        private String code;

        private String description;

        private BigDecimal depreciationRate;

        private Integer lifespanYears;

        private boolean active = true;
    }

    @Getter @Setter
    public static class UpdateRequest {

        private String name;

        private String code;

        private String description;

        private BigDecimal depreciationRate;

        private Integer lifespanYears;

        private Boolean active;
    }

    @Getter @Setter @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private String name;
        private String code;
        private String description;
        private BigDecimal depreciationRate;
        private Integer lifespanYears;
        private boolean active;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
