package com.hrms.asset.dto;

import com.hrms.asset.entity.AssetRequest.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class AssetRequestDto {

    @Getter @Setter
    public static class CreateRequest {

        @NotNull
        private UUID employeeId;

        private UUID categoryId;
        private UUID assetId;
        private String reason;
        private LocalDate requiredFrom;
        private LocalDate requiredUntil;
    }

    @Getter @Setter
    public static class UpdateRequest {

        private RequestStatus status;
        private UUID approvedBy;
        private String notes;
    }

    @Getter @Setter @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID employeeId;
        private UUID categoryId;
        private UUID assetId;
        private String reason;
        private LocalDate requiredFrom;
        private LocalDate requiredUntil;
        private RequestStatus status;
        private UUID approvedBy;
        private String notes;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
