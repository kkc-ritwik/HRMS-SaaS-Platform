package com.hrms.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class WorkflowDefinitionDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotBlank
        private String name;

        @NotBlank
        private String code;

        private String description;

        @NotBlank
        private String entityType;

        private String triggerEvent;

        private List<String> stepsConfig;

        private boolean active = true;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String name;

        private String code;

        private String description;

        private String entityType;

        private String triggerEvent;

        private List<String> stepsConfig;

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
        private String entityType;
        private String triggerEvent;
        private List<String> stepsConfig;
        private boolean active;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
