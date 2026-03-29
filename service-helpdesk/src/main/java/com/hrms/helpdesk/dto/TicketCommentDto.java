package com.hrms.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class TicketCommentDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull(message = "Ticket ID is required")
        private UUID ticketId;

        @NotNull(message = "Author ID is required")
        private UUID authorId;

        @NotBlank(message = "Comment is required")
        private String comment;

        private boolean internal = false;

        private String attachmentUrl;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String comment;

        private String attachmentUrl;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID ticketId;
        private UUID authorId;
        private String comment;
        private boolean internal;
        private String attachmentUrl;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
