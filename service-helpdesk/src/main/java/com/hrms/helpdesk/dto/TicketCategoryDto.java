package com.hrms.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class TicketCategoryDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Code is required")
        private String code;

        private String description;

        private int slaHours = 24;

        private UUID autoAssignTo;

        private boolean active = true;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String name;

        private String code;

        private String description;

        private Integer slaHours;

        private UUID autoAssignTo;

        private Boolean active;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private String name;
        private String code;
        private String description;
        private int slaHours;
        private UUID autoAssignTo;
        private boolean active;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
