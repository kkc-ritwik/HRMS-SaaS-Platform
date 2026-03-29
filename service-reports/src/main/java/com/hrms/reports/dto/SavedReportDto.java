package com.hrms.reports.dto;

import com.hrms.reports.entity.SavedReport.ReportFormat;
import com.hrms.reports.entity.SavedReport.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class SavedReportDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull
        private UUID definitionId;

        @NotBlank
        private String name;

        private List<String> filters;

        @NotNull
        private UUID generatedBy;

        @NotNull
        private ReportFormat fileFormat;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private ReportStatus status;
        private String fileUrl;
        private String errorMessage;
        private Instant generatedAt;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID definitionId;
        private String name;
        private List<String> filters;
        private UUID generatedBy;
        private Instant generatedAt;
        private String fileUrl;
        private ReportFormat fileFormat;
        private ReportStatus status;
        private String errorMessage;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
