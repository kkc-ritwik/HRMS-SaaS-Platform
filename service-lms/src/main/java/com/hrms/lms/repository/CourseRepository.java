package com.hrms.lms.repository;

import com.hrms.lms.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    Optional<Course> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<Course> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<Course> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    Page<Course> findByTenantIdAndStatusAndDeletedFalse(String tenantId, Course.CourseStatus status, Pageable pageable);

    List<Course> findByTenantIdAndMandatoryAndDeletedFalse(String tenantId, boolean mandatory);
}
