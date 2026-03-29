package com.hrms.lms.dto;

import com.hrms.lms.entity.CourseModule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class CourseModuleDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull(message = "Course ID is required")
        private UUID courseId;

        @NotBlank(message = "Title is required")
        private String title;

        private String description;
        private int orderIndex;
        private Integer durationMinutes;
        private CourseModule.ContentType contentType;
        private String contentUrl;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private UUID courseId;
        private String title;
        private String description;
        private Integer orderIndex;
        private Integer durationMinutes;
        private CourseModule.ContentType contentType;
        private String contentUrl;
        private CourseModule.ModuleStatus status;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID courseId;
        private String title;
        private String description;
        private int orderIndex;
        private Integer durationMinutes;
        private CourseModule.ContentType contentType;
        private String contentUrl;
        private CourseModule.ModuleStatus status;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
