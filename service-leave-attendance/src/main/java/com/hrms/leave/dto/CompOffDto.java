package com.hrms.leave.dto;

import com.hrms.leave.entity.CompOffRequest;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class CompOffDto {

    @Getter @Setter
    public static class RequestDto {
        @NotNull private LocalDate workedDate;
        private String reason;
        private LocalDate expiresAt;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID id;
        private UUID employeeId;
        private LocalDate workedDate;
        private String reason;
        private LocalDate expiresAt;
        private CompOffRequest.CompOffStatus status;
        private UUID approvedBy;
        private Instant createdAt;
    }
}
