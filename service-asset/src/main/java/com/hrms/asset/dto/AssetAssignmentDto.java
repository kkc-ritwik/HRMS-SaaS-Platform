package com.hrms.asset.dto;

import com.hrms.asset.entity.AssetAssignment.AssignmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class AssetAssignmentDto {

    @Getter @Setter
    public static class CreateRequest {

        @NotNull
        private UUID assetId;

        @NotNull
        private UUID employeeId;

        private UUID assignedBy;

        @NotNull
        private LocalDate assignedDate;

        private LocalDate expectedReturnDate;
        private String conditionAtAssignment;
    }

    @Getter @Setter
    public static class UpdateRequest {

        private LocalDate actualReturnDate;
        private String conditionAtReturn;
        private AssignmentStatus status;
    }

    @Getter @Setter @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID assetId;
        private UUID employeeId;
        private UUID assignedBy;
        private LocalDate assignedDate;
        private LocalDate expectedReturnDate;
        private LocalDate actualReturnDate;
        private String conditionAtAssignment;
        private String conditionAtReturn;
        private AssignmentStatus status;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
