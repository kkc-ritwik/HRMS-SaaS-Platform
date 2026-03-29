package com.hrms.reports.dto;

import com.hrms.reports.entity.DashboardWidget.WidgetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class DashboardWidgetDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull
        private UUID dashboardId;

        @NotNull
        private WidgetType widgetType;

        @NotBlank
        private String title;

        private String dataSource;
        private List<String> queryConfig;
        private List<String> displayConfig;
        private int positionX = 0;
        private int positionY = 0;
        private int width = 4;
        private int height = 3;
        private Integer refreshSeconds;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private WidgetType widgetType;
        private String title;
        private String dataSource;
        private List<String> queryConfig;
        private List<String> displayConfig;
        private Integer positionX;
        private Integer positionY;
        private Integer width;
        private Integer height;
        private Integer refreshSeconds;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID dashboardId;
        private WidgetType widgetType;
        private String title;
        private String dataSource;
        private List<String> queryConfig;
        private List<String> displayConfig;
        private int positionX;
        private int positionY;
        private int width;
        private int height;
        private Integer refreshSeconds;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
