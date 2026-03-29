package com.hrms.lms.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.lms.dto.CourseEnrollmentDto;
import com.hrms.lms.entity.CourseEnrollment;
import com.hrms.lms.repository.CourseEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseEnrollmentService {

    private final CourseEnrollmentRepository courseEnrollmentRepository;

    @Transactional
    public CourseEnrollmentDto.Response create(String tenantId, CourseEnrollmentDto.CreateRequest request, String currentUser) {
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setTenantId(tenantId);
        enrollment.setCreatedBy(currentUser);
        enrollment.setUpdatedBy(currentUser);
        enrollment.setCourseId(request.getCourseId());
        enrollment.setEmployeeId(request.getEmployeeId());
        enrollment.setEnrolledBy(request.getEnrolledBy());
        enrollment.setStatus(CourseEnrollment.EnrollmentStatus.ENROLLED);
        enrollment.setProgressPercentage(BigDecimal.ZERO);
        enrollment.setEnrolledAt(Instant.now());
        enrollment.setDueDate(request.getDueDate());
        return toResponse(courseEnrollmentRepository.save(enrollment));
    }

    @Transactional(readOnly = true)
    public CourseEnrollmentDto.Response getById(String tenantId, UUID id) {
        CourseEnrollment enrollment = courseEnrollmentRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseEnrollment", "id", id));
        return toResponse(enrollment);
    }

    @Transactional(readOnly = true)
    public Page<CourseEnrollmentDto.Response> list(String tenantId, Pageable pageable) {
        return courseEnrollmentRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CourseEnrollmentDto.Response> listAll(String tenantId) {
        return courseEnrollmentRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public CourseEnrollmentDto.Response update(String tenantId, UUID id, CourseEnrollmentDto.UpdateRequest request, String currentUser) {
        CourseEnrollment enrollment = courseEnrollmentRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseEnrollment", "id", id));
        if (request.getStatus() != null) enrollment.setStatus(request.getStatus());
        if (request.getProgressPercentage() != null) enrollment.setProgressPercentage(request.getProgressPercentage());
        if (request.getStartedAt() != null) enrollment.setStartedAt(request.getStartedAt());
        if (request.getCompletedAt() != null) enrollment.setCompletedAt(request.getCompletedAt());
        if (request.getScore() != null) enrollment.setScore(request.getScore());
        enrollment.setUpdatedBy(currentUser);
        return toResponse(courseEnrollmentRepository.save(enrollment));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        CourseEnrollment enrollment = courseEnrollmentRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseEnrollment", "id", id));
        enrollment.setDeleted(true);
        enrollment.setUpdatedBy(currentUser);
        courseEnrollmentRepository.save(enrollment);
    }

    @Transactional(readOnly = true)
    public Page<CourseEnrollmentDto.Response> listByEmployee(String tenantId, UUID employeeId, Pageable pageable) {
        return courseEnrollmentRepository.findByTenantIdAndEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, employeeId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CourseEnrollmentDto.Response> listByCourse(String tenantId, UUID courseId, Pageable pageable) {
        return courseEnrollmentRepository.findByTenantIdAndCourseIdAndDeletedFalse(tenantId, courseId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public CourseEnrollmentDto.Response updateProgress(String tenantId, UUID id, BigDecimal progressPercentage, String currentUser) {
        CourseEnrollment enrollment = courseEnrollmentRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseEnrollment", "id", id));
        enrollment.setProgressPercentage(progressPercentage);
        if (progressPercentage.compareTo(new BigDecimal("100")) == 0) {
            enrollment.setStatus(CourseEnrollment.EnrollmentStatus.COMPLETED);
            enrollment.setCompletedAt(Instant.now());
        } else if (progressPercentage.compareTo(BigDecimal.ZERO) > 0
                && enrollment.getStatus() == CourseEnrollment.EnrollmentStatus.ENROLLED) {
            enrollment.setStatus(CourseEnrollment.EnrollmentStatus.IN_PROGRESS);
            if (enrollment.getStartedAt() == null) {
                enrollment.setStartedAt(Instant.now());
            }
        }
        enrollment.setUpdatedBy(currentUser);
        return toResponse(courseEnrollmentRepository.save(enrollment));
    }

    private CourseEnrollmentDto.Response toResponse(CourseEnrollment enrollment) {
        return CourseEnrollmentDto.Response.builder()
                .id(enrollment.getId())
                .tenantId(enrollment.getTenantId())
                .courseId(enrollment.getCourseId())
                .employeeId(enrollment.getEmployeeId())
                .enrolledBy(enrollment.getEnrolledBy())
                .status(enrollment.getStatus())
                .progressPercentage(enrollment.getProgressPercentage())
                .enrolledAt(enrollment.getEnrolledAt())
                .startedAt(enrollment.getStartedAt())
                .completedAt(enrollment.getCompletedAt())
                .dueDate(enrollment.getDueDate())
                .score(enrollment.getScore())
                .createdBy(enrollment.getCreatedBy())
                .updatedBy(enrollment.getUpdatedBy())
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .build();
    }
}
