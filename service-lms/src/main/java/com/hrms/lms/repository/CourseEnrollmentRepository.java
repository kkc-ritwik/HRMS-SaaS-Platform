package com.hrms.lms.repository;

import com.hrms.lms.entity.CourseEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, UUID> {

    Optional<CourseEnrollment> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<CourseEnrollment> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<CourseEnrollment> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    Page<CourseEnrollment> findByTenantIdAndEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, UUID employeeId, Pageable pageable);

    Page<CourseEnrollment> findByTenantIdAndCourseIdAndDeletedFalse(String tenantId, UUID courseId, Pageable pageable);

    Optional<CourseEnrollment> findByTenantIdAndCourseIdAndEmployeeIdAndDeletedFalse(String tenantId, UUID courseId, UUID employeeId);
}
