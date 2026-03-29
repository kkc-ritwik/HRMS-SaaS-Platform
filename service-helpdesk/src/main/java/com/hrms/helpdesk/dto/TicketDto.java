package com.hrms.helpdesk.dto;

import com.hrms.helpdesk.entity.Ticket.Priority;
import com.hrms.helpdesk.entity.Ticket.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class TicketDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotBlank(message = "Title is required")
        private String title;

        private String description;

        private UUID categoryId;

        @NotNull(message = "Requester ID is required")
        private UUID requesterId;

        private UUID assigneeId;

        private Priority priority = Priority.MEDIUM;

        private Instant dueBy;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String title;

        private String description;

        private UUID categoryId;

        private UUID assigneeId;

        private Priority priority;

        private TicketStatus status;

        private Integer satisfactionRating;

        private Instant resolvedAt;

        private Instant closedAt;

        private Instant dueBy;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private String title;
        private String description;
        private UUID categoryId;
        private UUID requesterId;
        private UUID assigneeId;
        private Priority priority;
        private TicketStatus status;
        private Instant resolvedAt;
        private Instant closedAt;
        private Instant dueBy;
        private Integer satisfactionRating;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
