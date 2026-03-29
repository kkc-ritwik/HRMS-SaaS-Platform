package com.hrms.lms.dto;

import com.hrms.lms.entity.Course;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CourseDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotBlank(message = "Title is required")
        private String title;

        private String description;
        private String category;
        private UUID instructorId;
        private BigDecimal durationHours;
        private Course.CourseLevel level;
        private Course.CourseFormat format;
        private String thumbnailUrl;
        private List<String> tags;
        private boolean mandatory;
        private List<String> targetRoles;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String title;
        private String description;
        private String category;
        private UUID instructorId;
        private BigDecimal durationHours;
        private Course.CourseLevel level;
        private Course.CourseFormat format;
        private String thumbnailUrl;
        private Course.CourseStatus status;
        private List<String> tags;
        private Boolean mandatory;
        private List<String> targetRoles;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private String title;
        private String description;
        private String category;
        private UUID instructorId;
        private BigDecimal durationHours;
        private Course.CourseLevel level;
        private Course.CourseFormat format;
        private String thumbnailUrl;
        private Course.CourseStatus status;
        private List<String> tags;
        private boolean mandatory;
        private List<String> targetRoles;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
