package com.hrms.onboarding.dto;

import com.hrms.onboarding.entity.BuddyAssignment.AssignmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class BuddyAssignmentDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull(message = "Employee ID is required")
        private UUID employeeId;

        @NotNull(message = "Buddy ID is required")
        private UUID buddyId;

        private LocalDate startDate;

        private LocalDate endDate;

        private String notes;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private UUID employeeId;

        private UUID buddyId;

        private LocalDate startDate;

        private LocalDate endDate;

        private AssignmentStatus status;

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
        private UUID buddyId;
        private LocalDate startDate;
        private LocalDate endDate;
        private AssignmentStatus status;
        private String notes;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
