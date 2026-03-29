package com.hrms.lms.dto;

import com.hrms.lms.entity.CourseEnrollment;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class CourseEnrollmentDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull(message = "Course ID is required")
        private UUID courseId;

        @NotNull(message = "Employee ID is required")
        private UUID employeeId;

        private UUID enrolledBy;
        private LocalDate dueDate;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private CourseEnrollment.EnrollmentStatus status;
        private BigDecimal progressPercentage;
        private Instant startedAt;
        private Instant completedAt;
        private BigDecimal score;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID courseId;
        private UUID employeeId;
        private UUID enrolledBy;
        private CourseEnrollment.EnrollmentStatus status;
        private BigDecimal progressPercentage;
        private Instant enrolledAt;
        private Instant startedAt;
        private Instant completedAt;
        private LocalDate dueDate;
        private BigDecimal score;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
