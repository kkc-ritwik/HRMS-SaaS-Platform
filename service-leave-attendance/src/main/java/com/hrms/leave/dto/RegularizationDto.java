package com.hrms.leave.dto;

import com.hrms.leave.entity.RegularizationRequest;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class RegularizationDto {

    @Getter @Setter
    public static class RequestDto {
        @NotNull private LocalDate date;
        @NotNull private Instant correctedIn;
        @NotNull private Instant correctedOut;
        private String reason;
    }

    @Getter @Setter
    public static class ApprovalDto {
        private String comments;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID id;
        private UUID employeeId;
        private LocalDate date;
        private Instant originalIn;
        private Instant originalOut;
        private Instant correctedIn;
        private Instant correctedOut;
        private String reason;
        private RegularizationRequest.RegularizationStatus status;
        private UUID approvedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
