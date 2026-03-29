package com.hrms.reports.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ReportDefinitionDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotBlank
        private String name;

        @NotBlank
        private String code;

        private String description;
        private String category;
        private List<String> queryConfig;
        private List<String> parametersConfig;
        private List<String> outputFormats;
        private String scheduleCron;
        private UUID ownerId;
        private boolean active = true;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String name;
        private String code;
        private String description;
        private String category;
        private List<String> queryConfig;
        private List<String> parametersConfig;
        private List<String> outputFormats;
        private String scheduleCron;
        private UUID ownerId;
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
        private String description;
        private String category;
        private List<String> queryConfig;
        private List<String> parametersConfig;
        private List<String> outputFormats;
        private String scheduleCron;
        private UUID ownerId;
        private boolean active;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
