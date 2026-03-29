package com.hrms.lms.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "course_enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseEnrollment extends BaseEntity {

    public enum EnrollmentStatus { ENROLLED, IN_PROGRESS, COMPLETED, WITHDRAWN, EXPIRED }

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "enrolled_by")
    private UUID enrolledBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EnrollmentStatus status = EnrollmentStatus.ENROLLED;

    @Column(name = "progress_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal progressPercentage = BigDecimal.ZERO;

    @Column(name = "enrolled_at", nullable = false)
    private Instant enrolledAt = Instant.now();

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;
}
