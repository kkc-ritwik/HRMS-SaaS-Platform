package com.hrms.document.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class DocumentTemplateDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotBlank
        private String name;

        @NotBlank
        private String code;

        private String description;

        private String content;

        private String category;

        private boolean active = true;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String name;

        private String code;

        private String description;

        private String content;

        private String category;

        private Boolean active;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String name;
        private String code;
        private String description;
        private String content;
        private String category;
        private boolean active;
        private String tenantId;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
