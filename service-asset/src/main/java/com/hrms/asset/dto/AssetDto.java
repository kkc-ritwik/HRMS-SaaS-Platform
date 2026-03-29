package com.hrms.asset.dto;

import com.hrms.asset.entity.Asset.AssetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class AssetDto {

    @Getter @Setter
    public static class CreateRequest {

        @NotNull
        private UUID categoryId;

        @NotBlank
        private String name;

        @NotBlank
        private String code;

        private String serialNumber;
        private String make;
        private String model;
        private LocalDate purchaseDate;
        private BigDecimal purchasePrice;
        private BigDecimal currentValue;
        private String location;
        private LocalDate warrantyExpiry;
        private String notes;
    }

    @Getter @Setter
    public static class UpdateRequest {

        private UUID categoryId;
        private String name;
        private String code;
        private String serialNumber;
        private String make;
        private String model;
        private LocalDate purchaseDate;
        private BigDecimal purchasePrice;
        private BigDecimal currentValue;
        private AssetStatus status;
        private String location;
        private LocalDate warrantyExpiry;
        private String notes;
    }

    @Getter @Setter @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID categoryId;
        private String name;
        private String code;
        private String serialNumber;
        private String make;
        private String model;
        private LocalDate purchaseDate;
        private BigDecimal purchasePrice;
        private BigDecimal currentValue;
        private AssetStatus status;
        private String location;
        private LocalDate warrantyExpiry;
        private String notes;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
