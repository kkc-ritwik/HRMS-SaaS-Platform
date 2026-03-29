package com.hrms.lms.dto;

import com.hrms.lms.entity.Assessment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AssessmentDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull(message = "Course ID is required")
        private UUID courseId;

        @NotBlank(message = "Title is required")
        private String title;

        private String description;
        private int totalMarks = 100;
        private int passingMarks = 60;
        private Integer durationMinutes;
        private int attemptsAllowed = 3;
        private List<String> questions;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private UUID courseId;
        private String title;
        private String description;
        private Integer totalMarks;
        private Integer passingMarks;
        private Integer durationMinutes;
        private Integer attemptsAllowed;
        private Assessment.AssessmentStatus status;
        private List<String> questions;
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
        private int totalMarks;
        private int passingMarks;
        private Integer durationMinutes;
        private int attemptsAllowed;
        private Assessment.AssessmentStatus status;
        private List<String> questions;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
