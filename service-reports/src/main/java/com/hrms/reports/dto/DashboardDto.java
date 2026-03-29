package com.hrms.reports.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class DashboardDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotBlank
        private String name;

        private String description;

        @NotNull
        private UUID ownerId;

        private boolean shared = false;
        private List<String> sharedWith;
        private String theme;
        private boolean defaultDashboard = false;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String name;
        private String description;
        private Boolean shared;
        private List<String> sharedWith;
        private List<String> layoutConfig;
        private String theme;
        private Boolean defaultDashboard;
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
        private String description;
        private UUID ownerId;
        private boolean shared;
        private List<String> sharedWith;
        private List<String> layoutConfig;
        private String theme;
        private boolean defaultDashboard;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
