package com.hrms.asset.dto;

import com.hrms.asset.entity.AssetMaintenance.MaintenanceStatus;
import com.hrms.asset.entity.AssetMaintenance.MaintenanceType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class AssetMaintenanceDto {

    @Getter @Setter
    public static class CreateRequest {

        @NotNull
        private UUID assetId;

        @NotNull
        private MaintenanceType maintenanceType;

        private String description;
        private LocalDate scheduledDate;
        private BigDecimal cost;
        private String vendor;
        private String notes;
    }

    @Getter @Setter
    public static class UpdateRequest {

        private MaintenanceType maintenanceType;
        private String description;
        private LocalDate scheduledDate;
        private LocalDate completedDate;
        private BigDecimal cost;
        private String vendor;
        private MaintenanceStatus status;
        private String notes;
    }

    @Getter @Setter @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID assetId;
        private MaintenanceType maintenanceType;
        private String description;
        private LocalDate scheduledDate;
        private LocalDate completedDate;
        private BigDecimal cost;
        private String vendor;
        private MaintenanceStatus status;
        private String notes;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
