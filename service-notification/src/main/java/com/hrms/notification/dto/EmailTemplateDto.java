package com.hrms.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class EmailTemplateDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Code is required")
        private String code;

        @NotBlank(message = "Subject is required")
        private String subject;

        private String bodyHtml;

        private String bodyText;

        private List<String> variables;

        private String category;

        private boolean active = true;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String name;

        private String subject;

        private String bodyHtml;

        private String bodyText;

        private List<String> variables;

        private String category;

        private Boolean active;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        private UUID id;
        private String tenantId;
        private String name;
        private String code;
        private String subject;
        private String bodyHtml;
        private String bodyText;
        private List<String> variables;
        private String category;
        private boolean active;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
