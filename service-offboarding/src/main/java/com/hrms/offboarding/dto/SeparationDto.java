package com.hrms.offboarding.dto;

import com.hrms.offboarding.entity.Separation.SeparationStatus;
import com.hrms.offboarding.entity.Separation.SeparationType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class SeparationDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull(message = "Employee ID is required")
        private UUID employeeId;

        @NotNull(message = "Separation type is required")
        private SeparationType separationType;

        private LocalDate lastWorkingDate;

        private LocalDate noticeDate;

        private String reason;

        @NotNull(message = "Initiated by is required")
        private UUID initiatedBy;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private SeparationType separationType;

        private LocalDate lastWorkingDate;

        private LocalDate noticeDate;

        private String reason;

        private SeparationStatus status;

        private UUID approvedBy;

        private LocalDate finalSettlementDate;

        private String notes;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID employeeId;
        private SeparationType separationType;
        private LocalDate lastWorkingDate;
        private LocalDate noticeDate;
        private String reason;
        private SeparationStatus status;
        private UUID initiatedBy;
        private UUID approvedBy;
        private LocalDate finalSettlementDate;
        private String notes;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
