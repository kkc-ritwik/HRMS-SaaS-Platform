package com.hrms.offboarding.dto;

import com.hrms.offboarding.entity.KnowledgeTransfer.TransferStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class KnowledgeTransferDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull(message = "Separation ID is required")
        private UUID separationId;

        @NotNull(message = "From employee ID is required")
        private UUID fromEmployeeId;

        @NotNull(message = "To employee ID is required")
        private UUID toEmployeeId;

        @NotBlank(message = "Topic is required")
        private String topic;

        private String description;

        private String documentUrl;

        private LocalDate dueDate;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String topic;

        private String description;

        private String documentUrl;

        private LocalDate dueDate;

        private TransferStatus status;

        private Instant completedAt;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID separationId;
        private UUID fromEmployeeId;
        private UUID toEmployeeId;
        private String topic;
        private String description;
        private String documentUrl;
        private TransferStatus status;
        private LocalDate dueDate;
        private Instant completedAt;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
